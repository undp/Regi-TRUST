#updated to work with Python3. Libraries updated. See setup.py
import json
import logging
from urllib.parse import urlparse
import falcon
import gunicorn.app.base
from sqlalchemy.orm.exc import NoResultFound
from zonedb.models import AuthToken, SchemeClaim, TrustList, TrustListCert
from zonedb.master import refresh_zonefile, reload_master
from datetime import datetime

logging.basicConfig(filename='/tmp/logger.log', encoding='utf-8', level=logging.DEBUG)

def auth_zone(req):
    now = datetime.now()
    logging.debug(" ")
    logging.debug(now)
    logging.debug("starting request...")
    if not req.auth:
        logging.debug("exception: authorization")
        raise falcon.HTTPForbidden("403 Forbidden", "Authorization required")
    words = req.auth.split()
    if len(words) < 2:
        logging.debug("exception: token")
        raise falcon.HTTPForbidden("403 Forbidden", "Authorization required")
    if words[0].lower() != "bearer":
        logging.debug("exception: no word 'bearer' in authorization")
        raise falcon.HTTPForbidden("403 Forbidden", "Authorization required")
    try:
        token = req.context.session.query(AuthToken).filter_by(token=words[1]).first()
    except NoResultFound:
        logging.debug("exception: 'NoResultFound' when querying the DB")
        raise falcon.HTTPForbidden("403 Forbidden", "Authorization required")
    logging.debug("Token fetched: " + token.token)
    return token.zone


def load_json(req):
    if req.content_length > 0:
        try:
            return json.load(req.stream)
        except:
            raise falcon.HTTPBadRequest("400 Bad Request", "invalid body")
    else:
        return dict()

def decode_certs(cert_list):
    if isinstance(cert_list, dict):
        cert_list = (cert_list,)
    if not isinstance(cert_list, (tuple, list)):
        raise falcon.HTTPBadRequest(
            "400 Bad Request",
            "certificates must be a list"
        )
    res = list()
    for cert in cert_list:
        usage = cert.get("usage")
        selector = cert.get("selector")
        matching = cert.get("matching")
        try:
            data = cert["data"]
        except KeyError as e:
            raise falcon.HTTPBadRequest(
                "400 Bad Request",
                "'certificates': missing key '%s'" % e
            )
        try:
            res.append(TrustListCert.create(usage, selector, matching, data))
        except ValueError as e:
            raise falcon.HTTPBadRequest(
                "400 Bad Request",
                "%s" % e
            )
    return res


class Status(object):

    def on_get(self, req, resp):
        zone = auth_zone(req)
        doc = {
            'environment': req.context.environment.name,
            'zone': zone.apex
        }
        resp.body = json.dumps(doc, ensure_ascii=False)


class TrustListResource(object):
    def __init__(self, list_type):
        self.list_type = list_type

    def on_get(self, req, resp, scheme_name):
        zone = auth_zone(req)
        tl = req.context.session.query(TrustList) \
                .filter_by(
                    zone=zone, list_type=self.list_type, name=scheme_name
                ).one_or_none()
        if tl is None:
            logging.debug("TrustlistResource GET, empty")
            resp.body = json.dumps([], ensure_ascii=False)
        else:
            doc = dict(url=tl.url, certificates=list())
            for cert in tl.certs:
                doc["certificates"].append(cert.as_json())
            resp.body = json.dumps(doc, ensure_ascii=False)
            logging.debug("TrustlistResource GET, OK")

    def on_put(self, req, resp, scheme_name):
        zone = auth_zone(req)
        if not zone.contains_name(scheme_name):
            logging.debug("TrustlistResource PUT, domain name not in zone. Not found: "+scheme_name)
            raise falcon.HTTPNotFound(title="domain name not in zone")
        data = load_json(req)
        try:
            url = data["url"]
            cert = data["certificate"]
        except KeyError as e:
            logging.debug("PUT Trust list, bad request 400, missing key.")
            raise falcon.HTTPBadRequest(
                "400 Bad Request",
                "missing key '%s'" % e
            )
        if urlparse(url).scheme not in ('http', 'https'):
            logging.debug("PUT Trust list, bad request 400, URL must be HTTPS.")
            raise falcon.HTTPBadRequest(
                "400 Bad Request",
                "'url' must be a HTTPS URL"
            )
        cert_list = decode_certs(cert)
        self.delete(req, zone, scheme_name)
        trust_list = TrustList(
            zone=zone, name=scheme_name, list_type=self.list_type, url=url,
            certs=cert_list
        )
        try:
            trust_list.rr()
            for cert in trust_list.certs:
                cert.rr(trust_list)
        except:
            req.context.session.rollback()
            logging.debug("PUT Trust list, bad request 400, invalid data in content")
            raise falcon.HTTPBadRequest(
                "400 Bad Request",
                "invalid data in content"
            )
        req.context.session.add(trust_list)
        req.context.session.commit()
        refresh_zonefile(req.context.session, req.context.environment, zone)
        reload_master(req.context.environment)
        self.on_get(req, resp, scheme_name)
        logging.debug("TrustlistResource PUT, OK in scheme " + scheme_name)

    def on_delete(self, req, resp, scheme_name):
        zone = auth_zone(req)
        if self.delete(req, zone, scheme_name):
            resp.status = falcon.HTTP_204
        else:
            resp.status = falcon.HTTP_404

    def delete(self, req, zone, scheme_name):
        """Deletes a trust list entry.
        Returns whether there was an entry to delete.
        """
        tl = req.context.session.query(TrustList) \
            .filter_by(zone=zone, name=scheme_name, list_type=self.list_type) \
            .one_or_none()
        if tl is None:
            return False
        else:
            for cert in tl.certs:
                req.context.session.delete(cert)
            req.context.session.delete(tl)
            req.context.session.commit()
            refresh_zonefile(req.context.session, req.context.environment, zone)
            reload_master(req.context.environment)
            return True


class SchemeClaimResource(object):
    def on_get(self, req, resp, scheme_name):
        zone = auth_zone(req)
        claims = req.context.session.query(SchemeClaim) \
                    .filter_by(zone=zone, name=scheme_name) \
                    .all()
        if claims is None:
            logging.debug("SchemeClaimResource GET, empty")
            resp.body = json.dumps([], ensure_ascii=False)
        else:
            doc = dict(schemes=[])
            for item in claims:
                doc["schemes"].append(item.scheme)
            logging.debug("SchemeClaimResource GET, OK. " + scheme_name)
            resp.body = json.dumps(doc, ensure_ascii=False)

    def on_put(self, req, resp, scheme_name):
        zone = auth_zone(req)
        data = load_json(req)
        try:
            schemes = data["schemes"]
        except KeyError as e:
            logging.debug("PUT Scheme Claim Resource, bad request 400, missing key.")
            raise falcon.HTTPBadRequest(
                "400 Bad Request",
                "missing key '%s'" % e
            )
        if not isinstance(schemes, (list, tuple)):
            logging.debug("PUT Scheme Claim Resource, scheme must be a list")
            raise falcon.HTTPBadRequest(
                "400 Bad Request",
                "'schemes' must be a list"
            )
        for item in schemes:
            if not isinstance(item, str):
                logging.debug("PUT Scheme Claim Resource, scheme must be a list of strings")
                raise falcon.HTTPBadRequest(
                    "400 Bad Request",
                    "'schemes' must be a list of strings"
                )
            try:
                item.encode("ascii")
            except:
                logging.debug("PUT Scheme Claim Resource, scheme must be domain names")
                raise falcon.HTTPBadRequest(
                    "400 Bad Request",
                    "'schemes' must be domain names"
                )
            if len(item) > 255:
                logging.debug("PUT Scheme Claim Resource, scheme must be domain names")
                raise falcon.HTTPBadRequest(
                    "400 Bad Request",
                    "'schemes' must be domain names"
                )
        req.context.session.query(SchemeClaim) \
                   .filter_by(zone=zone, name=scheme_name) \
                   .delete()
        for item in schemes:
            claim = SchemeClaim(zone=zone, name=scheme_name, scheme=item)
            try:
                claim.rr()
            except:
                req.context.session.rollback()
                logging.debug("PUT Scheme Claim Resource, invalid data in content")
                raise falcon.HTTPBadRequest(
                    "400 Bad Request",
                    "invalid data in content"
                )
            req.context.session.add(claim)
        req.context.session.commit()
        refresh_zonefile(req.context.session, req.context.environment, zone)
        reload_master(req.context.environment)
        logging.debug("SchemeClaimResource PUT, OK. " + scheme_name)
        self.on_get(req, resp, scheme_name)

    def on_delete(self, req, resp, scheme_name):
        zone = auth_zone(req)
        res = req.context.session.query(SchemeClaim) \
                                 .filter_by(zone=zone, name=scheme_name) \
                                 .delete()
        req.context.session.commit()
        refresh_zonefile(req.context.session, req.context.environment, zone)
        reload_master(req.context.environment)
        if res == 0:
            resp.status = falcon.HTTP_404
        else:
            resp.status = falcon.HTTP_204


class ApiApplication(gunicorn.app.base.BaseApplication):
    def __init__(self, options):
        self.options = options
        super(ApiApplication, self).__init__()

    def load_config(self):
        self.cfg.set("bind", self.options.bind)

    def load(self):
        api = falcon.API(request_type=type(
            'Request',
            (falcon.Request,),
            dict(context_type=lambda rq: self.options)
        ))

        api.add_route('/status', Status())
        api.add_route(
            '/names/{scheme_name}/trust-list',
            TrustListResource("scheme")
        )
        api.add_route(
            '/names/{scheme_name}/translation',
            TrustListResource("translation")
        )
        api.add_route(
            '/names/{scheme_name}/schemes',
            SchemeClaimResource()
        )

        return api
