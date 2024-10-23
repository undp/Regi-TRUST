#! /usr/bin/env python2.7
#updated to work on Python3

import argparse
from os import path
import ldns
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from zonedb.api import ApiApplication
from zonedb.models import AuthToken, Base, Environment, Record, Zone
from zonedb import master
import logging
from datetime import datetime


logging.basicConfig(filename='/tmp/logger.log', encoding='utf-8', level=logging.DEBUG)

def get_config():
    """Produces the configuration."""
    parser = argparse.ArgumentParser(description='The LIGHTest zone manager.')
    
    # Global options.
    parser.add_argument("--database", "-d", action="store",
                        help="URL for then zones database",
                        default="sqlite:///zones.db")
    subparsers = parser.add_subparsers(help="sub-command help")

    Init.args(subparsers)
    AddEnvironment.args(subparsers)
    Resign.args(subparsers)
    AddZone.args(subparsers)
    AddRecord.args(subparsers)
    AddToken.args(subparsers)
    Server.args(subparsers)

    return parser.parse_args()


class Init:
    """Initializes the database."""
    @classmethod
    def args(cls, subparsers):
        sub = subparsers.add_parser("init", help=Init.__doc__)
        sub.add_argument("--force", "-f", action="store_true",
                         help="recreate all tables")
        sub.set_defaults(func=cls.run)

    @classmethod
    def run(cls, config):
        if config.force:
            print("Dropping existing database tables in '%s'" % config.database)
            Base.metadata.drop_all(config.engine)
        print("Creating database tables in `%s'" % config.database)
        Base.metadata.create_all(config.engine)


class AddEnvironment:
    """Adds a new server config."""

    @classmethod
    def args(cls, subparsers):
        sub = subparsers.add_parser("add-environment", help=cls.__doc__)

        sub.add_argument("--environment", "-e", action="store", required=True,
                         help="name of the new environment")
        sub.add_argument("--nsd-name", "-n", action="store", required=True,
                         help="host name of the primary name server")
        sub.add_argument("--nsd-conf", "-c", action="store", required=True,
                         help="path to the NSD config to be created")
        sub.add_argument("--nsd-reload", "-r", action="store", required=True,
                         help="command to reload NSD")
        sub.add_argument("--key-file", "-k", action="store", required=True,
                         help="path to a file for temporary key storage.") 

        sub.set_defaults(func=cls.run)

    @classmethod
    def run(cls, config):
        env = Environment(name=config.environment, nsd_name=config.nsd_name,
                          nsd_conf=path.abspath(config.nsd_conf),
                          nsd_reload=config.nsd_reload,
                          key_file=path.abspath(config.key_file))
        config.session.add(env)
        config.session.commit()
        master.refresh_master(config.session)

class Resign:
    """Resigns all zones in all environments."""

    @classmethod
    def args(cls, subparsers):
        sub = subparsers.add_parser("resign", help=cls.__doc__)

        sub.set_defaults(func=cls.run)

    @classmethod
    def run(cls, config):
        master.refresh_master(config.session)


class AddZone:
    """Adds a new zone."""

    @classmethod
    def args(cls, subparsers):
        sub = subparsers.add_parser("add-zone", help=cls.__doc__)
        
        sub.add_argument("--environment", "-e", action="store", required=True,
                         help="name of the environment to use")
        sub.add_argument("--apex", "-a", action="store", required=True,
                         help="apex domain name for the zone")
        sub.add_argument("--pattern", "-p", action="store", required=False,
                         help="the NSD pattern for the zone")

        sub.set_defaults(func=cls.run)

    @classmethod
    def run(cls, config):
        env = config.session.query(Environment) \
                    .filter_by(name=config.environment).one()
        zone = Zone.defaults(config.apex, env)
        if config.pattern:
            zone.pattern = config.pattern
        config.session.add(zone)
        zone.create_keys(config.session)
        config.session.commit()
        master.refresh_master(config.session)
        zone = config.session.query(Zone) \
                     .filter_by(environment=env, apex=config.apex).one()
        print(master.get_ds(config.session, env, zone))


class AddRecord:
    """Adds one or more records to a zone."""

    @classmethod
    def args(cls, subparsers):
        sub = subparsers.add_parser("add-record", help=cls.__doc__)

        sub.add_argument("--environment", "-e", action="store", required=True,
                         help="name of the environment to use")
        sub.add_argument("--apex", "-a", action="store", required=True,
                         help="apex domain name for the zone")
        sub.add_argument("--ttl", "-t", action="store", type=int, default=3600,
                         help="the TTL of the records")
        sub.add_argument("name", action="store",
                         help="the domain name of the records to be added")
        sub.add_argument("rtype", action="store",
                         help="the record type of the records to be added")
        sub.add_argument("data", action="store", nargs="+",
                         help="the data of the record (quoted if with space")

        sub.set_defaults(func=cls.run)

    @classmethod
    def run(cls, config):
        env = config.session.query(Environment) \
                    .filter_by(name=config.environment).one()
        zone = config.session.query(Zone) \
                    .filter_by(environment=env, apex=config.apex).one()
        for data in config.data:
            config.session.add(
                Record(zone=zone, name=config.name, rtype=config.rtype,
                       ttl=config.ttl, rdata=data)
            )
        config.session.commit()
        master.refresh_master(config.session)


class AddToken:
    """Creates a new authentication token."""

    @classmethod
    def args(cls, subparsers):
        sub = subparsers.add_parser("create-token", help=cls.__doc__)

        sub.add_argument("--environment", "-e", action="store", required=True,
                         help="name of the environment to use")
        sub.add_argument("name", action="store",
                         help="a name for the token to refer to it later")
        sub.add_argument("zone", action="store",
                         help="zone to allow access to")

        sub.set_defaults(func=cls.run)

    @classmethod
    def run(cls, config):
        env = config.session.query(Environment) \
                    .filter_by(name=config.environment).one()
        zone = config.session.query(Zone) \
                    .filter_by(environment=env, apex=config.zone).one()
        token = AuthToken.create(config.name, zone)
        config.session.add(token)
        config.session.commit()
        print(token.token)


class Server:
    """Runs the HTTP Server."""

    @classmethod
    def args(cls, subparsers):
        sub = subparsers.add_parser("server", help=cls.__doc__)

        sub.add_argument("--environment", "-e", action="store", required=True,
                         help="name of the environment to use")
        sub.add_argument("bind", action="store", default="127.0.0.1:8088",
                         nargs="?",
                         help="address:port to bind to")

        sub.set_defaults(func=cls.run)

    @classmethod
    def run(cls, config):
        config.environment = config.session.query(Environment) \
                                   .filter_by(name=config.environment).one()
        ApiApplication(config).run()


if __name__ == "__main__":
    logging.debug("******************configuration started******************")
    now = datetime.now()
    logging.debug(now)
    config = get_config()
    config.engine = create_engine(config.database)
    Session = sessionmaker(bind=config.engine)
    config.session = Session()
    logging.debug("before cofig.func")
    config.func(config)
    logging.debug("configuration ended")


