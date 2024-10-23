"""Provides classes definition and methods as well as database relationships for the Zone Manager"""
import logging
from base64 import b16decode
from os import path
from secrets import token_urlsafe

from ldns import LDNS_SIGN_ECDSAP256SHA256, ldns_key, ldns_rr
from sqlalchemy import Boolean, Column, ForeignKey, Integer, String, Text
from sqlalchemy.orm import declarative_base, relationship

Base = declarative_base()
LOG = logging.getLogger(__name__)


class Environment(Base):
    """Configuration Environment.
    Via this table, the configuration for different zones can be set. In
    general, there should only ever by one environment, but if you insist,
    you could use more.
    """
    __tablename__ = 'environment'

    # Base information
    id = Column(Integer, primary_key=True)
    name = Column(String, unique=True)
    key_file = Column(
        String,
        doc="""Path to a file that temporarily holds the private key.

        This is necessary due to some peculiarities with the library used.
        Basically, before using any private key, we have to write it to that
        file.
        """)

    # NSD configuration
    #
    nsd_name = Column(
        String,
        doc="Hostname of the name server.")
    nsd_conf = Column(
        String,
        doc="""Path to the NSD configuration to be created.
        
        Zonemanager produces a single NSD config file with all its
        information that must be included from NSD's actual config
        file via the "include: " directive.
        """)
    nsd_reload = Column(
        String,
        doc="""Command to run to reload NSD.""")


class Zone(Base):
    """A DNS zone."""
    __tablename__ = 'zone'

    # Zone information
    id = Column(Integer, primary_key=True)
    apex = Column(String, unique=True)
    environment_id = Column(Integer, ForeignKey('environment.id'))

    # NSD information
    path = Column(String)
    pattern = Column(String)

    # SOA rdata
    soa_ttl = Column(Integer)
    mname = Column(String)
    rname = Column(String)
    refresh = Column(Integer)
    retry = Column(Integer)
    expire = Column(Integer)
    minimum = Column(Integer)

    # Other data
    dnskey_ttl = Column(Integer)

    # Relationships
    environment = relationship("Environment", back_populates="zones")
    keys = relationship("ZoneKey", back_populates="zone")
    tokens = relationship("AuthToken", back_populates="zone")
    trust_lists = relationship("TrustList", back_populates="zone")
    scheme_claims = relationship("SchemeClaim", back_populates="zone")

    @classmethod
    def defaults(cls, apex, environment):
        """Creates a new zone value with almost everything set to defaults."""
        return cls(
            apex=apex, environment_id=environment.id,
            path=path.join(path.dirname(environment.nsd_conf), apex),
            pattern=None,
            soa_ttl=3600,
            mname=environment.nsd_name,
            rname=f"zonedb.{apex}",
            refresh=28800,
            retry=7200,
            expire=604800,
            minimum=3600,
            dnskey_ttl=3600,
        )

    def create_keys(self, session):
        """Creates a set of new keys for this zone.
        
        This creates a new KSK and a new ZSK with default parameters. For
        now, these are hard-coded.
        """
        private_ksk = ldns_key.new_frm_algorithm(
            LDNS_SIGN_ECDSAP256SHA256, 256
        )
        private_ksk.set_flags(257)
        private_zsk = ldns_key.new_frm_algorithm(
            LDNS_SIGN_ECDSAP256SHA256, 256
        )
        private_zsk.set_flags(256)
        ksk = Key(private_key=str(private_ksk))
        zsk = Key(private_key=str(private_zsk))
        session.add(ksk)
        session.add(zsk)
        session.add(ZoneKey(zone=self, key=ksk, ksk=True))
        session.add(ZoneKey(zone=self, key=zsk, ksk=False))

    def contains_name(self, name):
        """Returns whether the zone contains a certain domain name.
        It doesn't consider delegations.
        """
        return name.endswith(self.apex)


Environment.zones = relationship("Zone", order_by=Zone.id,
                                 back_populates="environment")


class Key(Base):
    """A key for zone signing."""
    __tablename__ = 'key'

    id = Column(Integer, primary_key=True)
    private_key = Column(
        String,
        doc="""The content of the private key file.""")

    zones = relationship("ZoneKey", back_populates="key")


class ZoneKey(Base):
    """A key assigned to a zone."""
    __tablename__ = 'zone_key'

    zone_id = Column(Integer, ForeignKey("zone.id"), primary_key=True)
    key_id = Column(Integer, ForeignKey("key.id"), primary_key=True)
    ksk = Column(Boolean)

    zone = relationship("Zone", back_populates="keys")
    key = relationship("Key", back_populates="zones")


class Record(Base):
    """Generic resource record in a zone."""
    __tablename__ = 'record'

    id = Column(Integer, primary_key=True)
    zone_id = Column(Integer, ForeignKey('zone.id'))
    name = Column(String)
    rtype = Column(String)
    ttl = Column(Integer)
    rdata = Column(String)

    zone = relationship("Zone", back_populates="records")

    def rr(self):
        return rr_from_str(str(
            "%s %i IN %s %s" % (
                self.name, self.ttl, self.rtype, self.rdata
            )
        ))


Zone.records = relationship("Record",
                            order_by=(Record.name, Record.rtype, Record.rdata),
                            back_populates="zone")


class AuthToken(Base):
    """Authentication tokens."""
    __tablename__ = 'auth_token'

    id = Column(Integer, primary_key=True)
    name = Column(String)
    token = Column(String)
    zone_id = Column(Integer, ForeignKey("zone.id"))

    zone = relationship("Zone", back_populates="tokens")

    @classmethod
    def create(cls, name, zone):
        return AuthToken(
            name=name,
            token=token_urlsafe(24),
            zone=zone,
        )


class TrustList(Base):
    """Trust list information assigned to a domain name."""
    __tablename__ = 'trust_list'

    id = Column(Integer, primary_key=True)
    zone_id = Column(Integer, ForeignKey('zone.id'))
    list_type = Column(String)
    name = Column(String)
    did = Column(String)

    zone = relationship(Zone, back_populates="trust_lists")

    def rr(self):
        return rr_from_str(str(
            "_%s._trust.%s 0 IN URI 10 1 \"%s\"" % (
                self.list_type, self.name, self.did
            )
        ))


class TrustListCert(Base):
    __tablename__ = "trust_list_cert"

    id = Column(Integer, primary_key=True)
    trust_list_id = Column(Integer, ForeignKey('trust_list.id'))
    usage = Column(Integer)
    selector = Column(Integer)
    matching = Column(Integer)
    data = Column(Text)

    @classmethod
    def create(cls, usage, selector, matching, data):
        if usage is None:
            usage = "dane-ee"
        if usage == "pkix-ta":
            usage = 0
        elif usage == "pkix-ee":
            usage = 1
        elif usage == "dane-ta":
            usage = 2
        elif usage == "dane-ee":
            usage = 3
        else:
            raise ValueError("invalid value '%s' in 'usage' field" % usage)

        if selector is None:
            selector = "spki"
        if selector == "cert":
            selector = 0
        elif selector == "spki":
            selector = 1
        else:
            raise ValueError(
                "invalid value '%s' in 'selector' field" % selector
            )

        if matching is None:
            matching = "sha256"
        if matching == "full":
            matching = 0
        elif matching == "sha256":
            matching = 1
        elif matching == "sha512":
            matching = 2
        else:
            raise ValueError(
                "invalid value '%s' in 'matching' field" % matching
            )
        try:
            b16decode(data)
        except Exception as exc:
            raise ValueError(f"Field {data=} must be base16 encoded") from exc
        return cls(
            usage=usage, selector=selector, matching=matching, data=data
        )

    def as_json(self):
        return dict(
            usage=self.usage,
            selector=self.selector,
            matching=self.matching,
            data=self.data
        )

    def rr(self, trust_list):
        return rr_from_str(str(
            "_%s._trust.%s 0 IN SMIMEA %i %i %i %s" % (
                trust_list.list_type, trust_list.name, self.usage,
                self.selector, self.matching, self.data
            )
        ))


class SchemeClaim(Base):
    __tablename__ = 'scheme_claim'

    id = Column(Integer, primary_key=True)
    zone_id = Column(Integer, ForeignKey('zone.id'))
    name = Column(String)
    scheme = Column(String)

    zone = relationship(Zone, back_populates="scheme_claims")

    def rr(self):
        return rr_from_str(str(
            "_scheme._trust.%s 0 IN PTR _scheme._trust.%s" % (
                self.name, self.scheme
            )
        ))


def rr_from_str(rr_str):
    try:
        return ldns_rr.new_frm_str(rr_str)
    except Exception as exc:
        LOG.debug("Exception creating resource record from string %r'.'%s'", rr_str, exc)
        raise exc
