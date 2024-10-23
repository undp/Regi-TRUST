#updated to work with Python3

"""Produces the zone master data."""

from time import time
import subprocess
import ldns
from zonedb.models import Environment


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

    open(environment.nsd_conf, "w").write(conf)


def refresh_zonefile(session, environment, zone):
    out = ldns.ldns_zone()
    soa = ldns.ldns_rr.new_frm_str(str(
        "%s %i IN SOA %s %s %i %i %i %i %i" % (
            zone.apex, zone.soa_ttl, zone.mname, zone.rname, time(),
            zone.refresh, zone.retry, zone.expire, zone.minimum
        )
    ))
    out.set_soa(soa)
    rrs = []
    for record in zone.records:
        try:
            rr = record.rr()
        except:
            continue
        rrs.append(rr)
        out.push_rr(rr)
    for claim in zone.scheme_claims:
        try:
            rr = claim.rr()
        except:
            continue
        rrs.append(rr)
        out.push_rr(rr)
    for trust_list in zone.trust_lists:
        try:
            rr = trust_list.rr()
        except:
            continue
        rrs.append(rr)
        out.push_rr(rr)
        for cert in trust_list.certs:
            try:
                rr = cert.rr(trust_list)
            except:
                continue
            rrs.append(rr)
            out.push_rr(rr)
    (hold, key_list) = load_key_list(session, environment, zone)
    for key in key_list.keys():
        rr = key.key_to_rr()
        key.set_keytag(ldns.ldns_calc_keytag(rr))
        rrs.append(rr)
        out.push_rr(rr)
    signed = out.sign(key_list)
    signed.print_to_file(open(zone.path, "w"))
    out.set_soa(None)


def load_key_list(session, environment, zone):
    """Loads the keys for a zone and returns an ldns_key_list."""
    res = ldns.ldns_key_list()
    hold = []
    for key in zone.keys:
        key = load_key(session, environment, zone, key.key, key.ksk)
        hold.append(key)
        res.push_key(key)
    return (hold, res)


def load_key(session, environment, zone, key, ksk):
    open(environment.key_file, "w").write(key.private_key)
    res = ldns.ldns_key.new_frm_fp(open(environment.key_file, "r"))
    res.set_flags(257 if ksk else 256)
    res.set_origttl(zone.dnskey_ttl)
    res.set_pubkey_owner(ldns.ldns_dname(str(zone.apex)))
    res.set_use(True)
    return res

def get_ds(session, environment, zone):
    res = ""
    (hold, key_list) = load_key_list(session, environment, zone)
    for key in key_list.keys():
        if key.flags() == 257:
            rr = key.key_to_rr()
            ds = ldns.ldns_key_rr2ds(rr, ldns.LDNS_SHA256)
            if res:
                res += '\n'
            res += str(ds)
    return res

