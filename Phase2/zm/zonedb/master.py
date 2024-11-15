"""Produces the zone master data."""
import logging
import subprocess
from pathlib import Path
from time import time

import ldns

from zonedb.models import Environment

LOG = logging.getLogger(__name__)


def refresh_master(session):
    """Recreates all master data for the zone data in the database."""
    for env in session.query(Environment):
        refresh_environment(session, env)
        reload_master(env)


def reload_master(environment):
    subprocess.call(environment.nsd_reload, shell=True)


def refresh_environment(session, environment):
    """Refreshes the given environment."""
    conf = ""
    for zone in environment.zones:
        
        conf += "zone:\n"
        conf += "   name: %s\n" % zone.apex
        conf += "   zonefile: %s\n" % zone.path
        if zone.pattern:
            conf += "   include-pattern: %s\n" % zone.pattern
        conf += "\n"

        refresh_zonefile(session, environment, zone)

    Path(environment.nsd_conf).write_bytes(conf.encode())


def refresh_zonefile(session, environment, zone):
    out = ldns.ldns_zone()
    record = str(
        "%s %i IN SOA %s %s %i %i %i %i %i" % (
            zone.apex, zone.soa_ttl, zone.mname.split('.')[0], zone.rname.split('.')[0], time(),
            zone.refresh, zone.retry, zone.expire, zone.minimum
        )
    )
    soa = ldns.ldns_rr.new_frm_str(record)
    out.set_soa(soa)
    rrs = []
    for record in zone.records:
        try:
            rr = record.rr()
        except Exception as exc:
            LOG.debug('skip record because %s', exc)
            continue
        rrs.append(rr)
        out.push_rr(rr)
    for claim in zone.scheme_claims:
        try:
            rr = claim.rr()
        except Exception as exc:
            LOG.debug('skip claim because %s', exc)
            continue
        rrs.append(rr)
        out.push_rr(rr)
    for trust_list in zone.trust_lists:
        try:
            rr = trust_list.rr()
        except Exception as exc:
            LOG.debug('skip trust_list because %s', exc)
            continue
        rrs.append(rr)
        out.push_rr(rr)
    (_, key_list) = load_key_list(session, environment, zone)
    for key in key_list.keys():
        rr = key.key_to_rr()
        key.set_keytag(ldns.ldns_calc_keytag(rr))
        rrs.append(rr)
        out.push_rr(rr)
    signed = out.sign(key_list)

    with Path(zone.path).open("w") as print_to_file:
        signed.print_to_file(print_to_file)

    out.set_soa(None)


def load_key_list(session, environment, zone):
    """Loads the keys for a zone and returns a ldns_key_list."""
    res = ldns.ldns_key_list()
    hold = []
    for key in zone.keys:
        key = load_key(session, environment, zone, key.key, key.ksk)
        hold.append(key)
        res.push_key(key)
    return hold, res


def load_key(_session, environment, zone, key, ksk):
    Path(environment.key_file).write_bytes(key.private_key.encode())

    with Path(environment.key_file).open("r") as key_file:
        res = ldns.ldns_key.new_frm_fp(key_file)
        res.set_flags(257 if ksk else 256)
        res.set_origttl(zone.dnskey_ttl)
        res.set_pubkey_owner(ldns.ldns_dname(str(zone.apex)))
        res.set_use(True)

    return res


def get_ds(session, environment, zone):
    res = ""
    (_, key_list) = load_key_list(session, environment, zone)
    for key in key_list.keys():
        if key.flags() == 257:
            rr = key.key_to_rr()
            ds = ldns.ldns_key_rr2ds(rr, ldns.LDNS_SHA256)
            if res:
                res += '\n'
            res += str(ds)
    return res
