"""Zone Manager server"""
from typing import Any

import json
import logging
import subprocess
from pathlib import Path

import falcon
import falcon.errors
import gunicorn.app.base
import jwt
import requests
from cryptography.hazmat.primitives import serialization
from jwt.algorithms import RSAAlgorithm
from sqlalchemy.orm.exc import NoResultFound

from zonedb.master import refresh_zonefile, reload_master
from zonedb.models import SchemeClaim, TrustList, TrustListCert, Zone
from zonedb.schemas import (SchemeResponseSchema, StatusSchema,
                            TrustListResponseSchema, ViewZoneResponseSchema)

LOG = logging.getLogger(__name__)

_AUTH_CONF_RESOLVED = (Path(__file__).parent / "../auth.conf").resolve()
AUTH_CONF = _AUTH_CONF_RESOLVED.read_text()

config = {}

exec(AUTH_CONF, config)


def auth_zone(req) -> Zone:
    '''
    if not req.auth:
        raise falcon.errors.HTTPForbidden(
            title="401 Unauthorized",
            description="Authorization as bearer token required"
        )
    words = req.auth.split()
    if len(words) < 2:
        raise falcon.errors.HTTPForbidden(
            title="401 Unauthorized",
            description="Authorization as bearer token required"
        )
    if words[0].lower() != "bearer":
        raise falcon.errors.HTTPForbidden(
            title="401 Unauthorized",
            description="Authorization as bearer token required"
        )

    #shouldVerifyCerts = not config['ALLOW_UNSAFE_SSL'] is 'true'
    shouldVerifyCerts = config['ALLOW_UNSAFE_SSL'] != 'true'
    if not shouldVerifyCerts:
        logging.warning('Skipping Cert validation for OIDC urls!')
    else:
        logging.info('Only allowing valid Certs!')

    issuer= requests.get(f"{config['ISSUER_URL']}/.well-known/openid-configuration", timeout=10, verify=shouldVerifyCerts)
    if issuer.status_code == 200:
        jwks_uri = issuer.json()['jwks_uri']
        print(jwks_uri)
    else:
        LOG.error("Token verification failed: token issuer responded with code %s", issuer.status_code)
        raise falcon.errors.HTTPUnauthorized(description="JWT Token validation failed: token issuer responded with code %s" % issuer.status_code)
    response = requests.get(jwks_uri, timeout=10, verify=shouldVerifyCerts)
    jwks = response.json()

    public_keys = {}
    for jwk in jwks['keys']:
        kid = jwk['kid']
        public_key = RSAAlgorithm.from_jwk(jwk)
        public_key_pem = public_key.public_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PublicFormat.SubjectPublicKeyInfo
        )
        public_keys[kid] = public_key_pem

    token = words[1]
    try:
        unverified_header = jwt.get_unverified_header(token)
    except jwt.PyJWTError as e:
        LOG.error("Token verification failed: %s", str(e))
        raise falcon.errors.HTTPUnauthorized(description="JWT Token validation failed: %s" % str(e))
    kid = unverified_header['kid']
    public_key_pem = public_keys.get(kid)

    if public_key_pem is None:
        raise falcon.errors.HTTPUnauthorized(description="No public key found for the 'kid' in token.")

    try:
        jwt.decode(token, public_key_pem, algorithms=["RS256"], audience=config['CLIENT_ID'])
    except jwt.PyJWTError as e:
        LOG.error("Token verification failed: %s", str(e))
        raise falcon.errors.HTTPUnauthorized(description="JWT Token validation failed: %s" % str(e))
    '''
    try:
        zone = req.context.session.query(Zone).first()
    except NoResultFound as no_result:
        raise falcon.errors.HTTPNotFound("404 no zone found", "No zone has been configured.") from no_result
    return zone


def load_json(req, run_with_wsgi: bool = False) -> dict[str, Any]:
    if req.content_length > 0:
        if run_with_wsgi:
            # https://falcon.readthedocs.io/en/stable/api/request_and_response_wsgi.html#falcon.Request.bounded_stream
            stream = req.bounded_stream
        else:
            stream = req.stream

        try:
            return json.load(stream)
        except Exception as exc:
            raise falcon.errors.HTTPBadRequest("400 Bad Request", "invalid body") from exc

    return {}


def decode_certs(cert_list):
    if isinstance(cert_list, dict):
        cert_list = (cert_list,)
    if not isinstance(cert_list, (tuple, list)):
        raise falcon.errors.HTTPBadRequest("400 Bad Request", "certificates must be a list")
    res = list()
    for cert in cert_list:
        usage = cert.get("usage")
        selector = cert.get("selector")
        matching = cert.get("matching")
        try:
            data = cert["data"]
        except KeyError as exc:
            raise falcon.errors.HTTPBadRequest("400 Bad Request", f"'certificates': missing key '{exc}'")
        try:
            res.append(TrustListCert.create(usage, selector, matching, data))
        except ValueError as exc:
            raise falcon.errors.HTTPBadRequest(f"400 Bad Request: {exc}")
    return res


class Status:
    def on_get(self, req, resp):
        """Status and health check. Checks 3 dependencies: zone manager server, nsd server and database.
        ---
        description: Get zone manager service status
        tags: 
            - Health Check
        responses:
            200:
                description: Status and healthcheck
                schema: StatusSchema
            401: 
                description: Unauthorized, not valid token
        """
        database_status = "OK"
        apex = "None"
        try:
            zone = req.context.session.query(Zone).first()
            apex = zone.apex
        except NoResultFound:
            LOG.error('no_result', exc_info=True)
            database_status = "No connection to database"
        system_status = "OK"
        doc = {
            "Zone": apex,
            "status": system_status,
            "dependencies": {
                "Zone_Manager_server_status": "OK",
                "Database_status": database_status,
                "NSD_server_status": "OK"
            }
        }

        status = subprocess.run(
            args=["service", "nsd", "status"],
            capture_output=True,
            text=True,
            check=False
        )
        if status.returncode == 0:  # nsd is running
            doc["dependencies"]["NSD_server_status"] = "OK"
        else:
            doc["status"] = "Dependency unavailable"
            doc["dependencies"]["NSD_server_status"] = "NSD server is not running"
        resp.text = json.dumps(doc, ensure_ascii=False)


class TrustListResource:
    def __init__(self, list_type: str, run_with_wsgi: bool) -> None:
        self._run_with_wsgi = run_with_wsgi
        self.list_type = list_type

    def on_get(self, req, resp, scheme_name):
        """Get a trust list pointer (did)
        ---
        description: Get trust list pointer
        security:
            - jwt_auth: []        
        tags: 
            - Trust Lists (DIDs)
        responses:
            200:
                description: Trust list pointer
            401: 
                description: Unauthorized, not valid token
            404:
                description: Not found. Non existing trust list framework
        """
        zone = auth_zone(req)
        tl = (
            req.context.session.query(TrustList)
            .filter_by(zone=zone, list_type=self.list_type, name=scheme_name)
            .one_or_none()
        )
        if tl is None:
            resp.text = json.dumps([], ensure_ascii=False)
        else:
            resp.text = json.dumps({"did": tl.did}, ensure_ascii=False)

    def on_put(self, req, resp, scheme_name):
        """Publish a trust list pointer (did)
        ---
        description: Publish trust list pointer
        tags: 
            - Trust Lists (DIDs)
        security:
            - jwt_auth: []  
        responses:
            200:
                description: Publish trust list pointer
                schema: TrustListResponseSchema
            401: 
                description: Unauthorized, not valid token
        """
        zone = auth_zone(req)
        scheme_claim = (
            req.context.session.query(SchemeClaim)
            .filter_by(zone=zone, scheme=scheme_name)
            .one_or_none()
        )
        if scheme_claim is None:
            raise falcon.errors.HTTPNotFound(title=scheme_name + " is not a valid trust scheme")
        if not zone.contains_name(scheme_name):
            LOG.debug("Trustlist PUT, domain name not in zone. Not found: %s", scheme_name)
            raise falcon.errors.HTTPNotFound(title=scheme_name + ": domain name not in current zone")
        data = load_json(req, self._run_with_wsgi)
        try:
            did = data["did"]
        except KeyError as exc:
            raise falcon.errors.HTTPBadRequest("400 Bad Request", f"missing key '{exc}'")
        if not did.startswith("did:"):
            raise falcon.errors.HTTPBadRequest("400 Bad Request", "Request must contain a valid did")
        self.delete(req, zone, scheme_name)
        trust_list = TrustList(zone=zone, name=scheme_name, list_type=self.list_type, did=did)
        try:
            trust_list.rr()
        except Exception as exc:
            req.context.session.rollback()
            raise falcon.errors.HTTPBadRequest("400 Bad Request", "invalid data in content") from exc
        req.context.session.add(trust_list)
        req.context.session.commit()
        refresh_zonefile(req.context.session, req.context.environment, zone)
        reload_master(req.context.environment)
        LOG.debug("Trust list record published. Scheme name: %s", scheme_name)
        self.on_get(req, resp, scheme_name)
        LOG.debug("TrustlistResource PUT, OK in scheme %s", scheme_name)

    def on_delete(self, req, resp, scheme_name):
        """Delete a trust list pointer (did)
        ---
        description: Delete trust list pointer
        tags: 
            - Trust Lists (DIDs)
        security:
            - jwt_auth: []  
        responses:
            204:
                description: Pointer to trust list removed
            401: 
                description: Unauthorized, not valid token
            404: 
                description: Not found
        """
        zone = auth_zone(req)
        if self.delete(req, zone, scheme_name):
            resp.status = falcon.HTTP_204
            LOG.debug("Trust list record deleted. Scheme name: %s", scheme_name)
        else:
            resp.status = falcon.status_codes.HTTP_404

    def delete(self, req, zone, scheme_name):
        tl = (
            req.context.session.query(TrustList)
            .filter_by(zone=zone, name=scheme_name, list_type=self.list_type)
            .one_or_none()
        )
        if tl is None:
            return False
        else:
            req.context.session.delete(tl)
            req.context.session.commit()
            refresh_zonefile(req.context.session, req.context.environment, zone)
            reload_master(req.context.environment)
            return True


class SchemeClaimResource:
    def __init__(self, run_with_wsgi: bool) -> None:
        self._run_with_wsgi = run_with_wsgi

    def on_get(self, req, resp, scheme_name):
        """Get a trust Framework pointer
        ---
        description: Get trust Framework pointer
        tags: 
            - Schemes (Trust Frameworks)
        security:
            - jwt_auth: []  
        responses:
            200:
                description: Trust Framework pointer
                schema: SchemeResponseSchema
            401: 
                description: Unauthorized, not valid token
            404:
                description: Not found. Non existing trust Framework
        """
        zone = auth_zone(req)
        claims = (
            req.context.session.query(SchemeClaim)
            .filter_by(zone=zone, name=scheme_name)
            .all()
        )
        if claims is None:
            LOG.debug("SchemeClaimResource GET, empty")
            resp.text = json.dumps([], ensure_ascii=False)
        else:
            doc = dict(schemes=[])
            for item in claims:
                doc["schemes"].append(item.scheme)
            LOG.debug("SchemeClaimResource GET, OK. %s", scheme_name)
            resp.text = json.dumps(doc, ensure_ascii=False)

    def on_put(self, req, resp, scheme_name):
        """Publish a trust Framework pointer
        ---
        description: Publish trust Framework pointer
        tags: 
            - Schemes (Trust Frameworks)
        security:
            - jwt_auth: []  
        responses:
            200:
                description: Trust Framework pointer publication
                schema: SchemeResponseSchema
            401: 
                description: Unauthorized, not valid token
        """
        zone = auth_zone(req)
        data = load_json(req, self._run_with_wsgi)

        try:
            schemes = data["schemes"]
        except KeyError as exc:
            raise falcon.errors.HTTPBadRequest("400 Bad Request", f"missing key '{exc}'")
        if not isinstance(schemes, (list, tuple)):
            raise falcon.errors.HTTPBadRequest("400 Bad Request", "'schemes' must be a list")
        if not zone.contains_name(scheme_name):
            LOG.debug("Trustscheme PUT, domain name not in zone. Not found: %s", scheme_name)
            raise falcon.errors.HTTPNotFound(title=scheme_name + ": domain name not in current zone")        
        if len(data["schemes"]) != len(set(data["schemes"])):
            raise falcon.errors.HTTPBadRequest(
                title="400 Bad Request",
                description="'schemes' can not contain repeated elements"
            )
        for item in schemes:
            if not isinstance(item, str):
                raise falcon.errors.HTTPBadRequest(
                    title="400 Bad Request",
                    description="'schemes' must be list of strings"
                )
            try:
                item.encode("ascii")
            except Exception as exc:
                raise falcon.errors.HTTPBadRequest(
                    "400 Bad Request", "'schemes' must be domain names") from exc
            if len(item) > 255:
                raise falcon.errors.HTTPBadRequest(
                    "400 Bad Request", "'schemes' must be domain names")
        req.context.session.query(SchemeClaim).filter_by(
            zone=zone, name=scheme_name
        ).delete()
        for item in schemes:
            claim = SchemeClaim(zone=zone, name=scheme_name, scheme=item)
            try:
                claim.rr()
            except Exception as exc:
                req.context.session.rollback()
                raise falcon.errors.HTTPBadRequest("400 Bad Request", "invalid data in content") from exc
            req.context.session.add(claim)
        req.context.session.commit()
        refresh_zonefile(req.context.session, req.context.environment, zone)
        reload_master(req.context.environment)
        LOG.debug("Trust scheme record published. Scheme name: %s", scheme_name)
        self.on_get(req, resp, scheme_name)

    def on_delete(self, req, resp, scheme_name):
        """Delete a trust framework pointer
        ---
        description: Delete trust list framework
        tags: 
            - Schemes (Trust Frameworks)
        security:
            - jwt_auth: []  
        responses:
            204:
                description: Pointer to trust framework removed
            401: 
                description: Unauthorized, not valid token
            404: 
                description: Not found. Non-existing framework
        """
        zone = auth_zone(req)
        fedpointers = req.context.session.query(SchemeClaim).filter_by(zone=zone, name=scheme_name).all()
        res = (
            req.context.session.query(SchemeClaim)
            .filter_by(zone=zone, name=scheme_name)
            .delete()
        )
        for fedpointer in fedpointers:
            (req.context.session.query(TrustList)
                .filter_by(zone=zone, name=fedpointer.scheme)
                .delete())
        req.context.session.commit()
        refresh_zonefile(req.context.session, req.context.environment, zone)
        reload_master(req.context.environment)
        if res == 0:
            resp.status = falcon.status_codes.HTTP_404
        else:
            resp.status = falcon.status_codes.HTTP_204
            LOG.debug("Trust scheme record deleted. Scheme name: %s", scheme_name)


class ViewZone:
    def on_get(self, req, resp):
        """Visualize the zone: Schemes (trust frameworks),
        subschemes (trusted external trust frameworks) its dids (pointers to trust lists).
        ---
        description: Visualization of zone
        tags: 
            - View Zone
        security:
            - jwt_auth: []  
        responses:
            200:
                description: Zone visualization
                schema: ViewZoneResponse
            401: 
                description: Unauthorized, not valid token
        """             
        auth_zone(req)
        zones = req.context.session.query(Zone).all()
        all_zones = []
        for zone in zones:
            current_zone = {"id": zone.id, "apex": zone.apex, "schemes": []}
            schemes = (
                req.context.session.query(SchemeClaim).filter_by(zone_id=zone.id).all()
            )
            trust_lists = req.context.session.query(TrustList).all()
            unique_schemes = set(scheme.name for scheme in schemes)
            for unique_scheme in unique_schemes:
                top_scheme = {"name": unique_scheme, "subSchemes": []}
                for scheme in schemes:
                    if scheme.name == unique_scheme:
                        subscheme = scheme.scheme
                        did = ""
                        for tl in trust_lists:
                            if tl.name == subscheme:
                                did = tl.did
                        el = {"subscheme": subscheme, "trustListDid": did}
                        top_scheme["subSchemes"].append(el)
                current_zone["schemes"].append(top_scheme)
            all_zones.append(current_zone)
        response_object = {"zones": all_zones}
        resp.text = json.dumps(response_object, ensure_ascii=False)


class ApiApplication(gunicorn.app.base.BaseApplication):
    def __init__(self, options):
        self.options = options
        super().__init__()

    def load_config(self):
        self.cfg.set("bind", self.options.bind)

    def load(self):
        api = falcon.App(
            request_type=type(
                "Request", (falcon.Request,), dict(context_type=lambda rq: self.options)
            )
        )
        routes = {
            "status" : Status(),
            "trust_list" : TrustListResource(
                list_type="scheme",
                run_with_wsgi=self.options.run_with_wsgi
            ),
            "scheme_claim" : SchemeClaimResource(
                run_with_wsgi=self.options.run_with_wsgi
            ),
            "view_zone" : ViewZone()
        }

        api.add_route("/status", routes["status"])
        api.add_route("/names/{scheme_name}/trust-list", routes["trust_list"])
        api.add_route("/names/{scheme_name}/schemes", routes["scheme_claim"])
        api.add_route("/view-zone", routes["view_zone"])

        if self.options.swagger:
            self.swagger(api,routes)

        return api

    def swagger(self,api,routes):
        from apispec import APISpec
        from apispec.ext.marshmallow import MarshmallowPlugin
        from falcon_apispec import FalconPlugin

        spec = APISpec(
            title='Eclipse XFSC TRAIN DNS Trustzone Manager',
            version='1.0.0',
            openapi_version='2.0',
            plugins=[
                FalconPlugin(api),
                MarshmallowPlugin(),
            ],
            securityDefinitions={
                'jwt_auth': {
                    'type': 'apiKey',
                    'name': 'Authorization',
                    'in': 'header',
                    'description': 'JWT Authorization header using the Bearer scheme. Example: "Authorization: Bearer {token}"',
                }
            }            
        )

        spec.components.schema('StatusResponse', schema=StatusSchema)
        spec.components.schema('ViewZoneResponse', schema=ViewZoneResponseSchema)
        spec.components.schema('SchemesResponse', schema=SchemeResponseSchema)
        spec.components.schema('TrustListResponse', schema=TrustListResponseSchema)
        spec.path(resource=routes["status"])
        spec.path(resource=routes["trust_list"])
        spec.path(resource=routes["scheme_claim"])
        spec.path(resource=routes["view_zone"])

        spec_yaml = spec.to_yaml()
        LOG.debug("writing yaml swagger spec into " + self.options.swagger)
        with open(self.options.swagger, 'w') as file:
            file.write(spec_yaml)
