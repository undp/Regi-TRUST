#! /usr/bin/env python2.7

from distutils.core import setup

setup(
    name='zonemanager',
    version='0.1',
    description='DNS Zone Manager for LIGHTest',
    author='Martin Hoffmann',
    author_email='martin@nlnetlabs.nl',
    url='https://www.lightest.eu/',
    packages=['zonedb'],
    scripts=['zonemanager.py'],
    install_requires=[
        'falcon==3.1.1',
        'gunicorn==19.9.0',
        'SQLAlchemy==2.0.1'
    ],
)
