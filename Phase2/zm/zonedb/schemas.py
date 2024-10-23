"""Provides schema definitions for the documenting of the API endpoints as openapi specification"""

from marshmallow import Schema, fields


class DependenciesSchema(Schema):
    Zone_Manager_server_status = fields.Str()
    Database_status = fields.Str()
    NSD_server_status = fields.Str()


class StatusSchema(Schema):
    zone = fields.Str()
    status = fields.Str()
    dependencies = fields.Nested(DependenciesSchema)


class SubSchemeSchema(Schema):
    subscheme = fields.Str(required=True)
    trustListDid = fields.Str(required=True)


class SchemeSchema(Schema):
    name = fields.Str(required=True)
    subSchemes = fields.List(fields.Nested(SubSchemeSchema), required=True)


class ZoneSchema(Schema):
    id = fields.Str(required=True)
    apex = fields.Str(required=True)
    schemes = fields.List(fields.Nested(SchemeSchema), required=True)


class ViewZoneResponseSchema(Schema):
    zones = fields.List(fields.Nested(ZoneSchema), required=True)


class SchemeResponseSchema(Schema):
    schemes = fields.List(fields.String)


class TrustListResponseSchema(Schema):
    did = fields.Str(required=True)
