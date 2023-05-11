# DNS ZoneManager for Regi-TRUST

Zone Manager setup for the project [Regi-TRUST](https://github.com/undp/Regi-TRUST)

### Disclaimer

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND. This software
is the output of combined research efforts. It was developed as proof-of-concept
to explore, test & verify various components created in the context of LIGHTest
and other projects. It can thus be used as a reference implementation.

# ZoneManager

This is the Regi-TRUST Zone Manager, a simple REST server that maintains and
signs the DNS zones used by the Regi-TRUST framework for publishing trust
data.

It was written in Python 2, updated to work with Python 3.10 using Falcon, gunicorn, LDNS, and SQL Alchemy.
See `setup.py` for the exact version we’ve used.

Zone Manager consists of a Python module, `zonedb`, and a executable
script, `zonemanager.py` that uses the module to provide the server.

## Installation

If your are running a Debian-based system the Zone Manager can be installed by running:

```
pip3 install git+https://gitlab.cc-asp.fraunhofer.de/essif_dev_internal/zone-manager.git
```

This will install the libraries specified in `setup.py`

On non-Debian systems, you might need to install dependencies separately.

## Setup

Please see the [demo setup](docs/install-demo.md) for instructions.

# Licence

- Apache License 2.0 (see [LICENSE](./LICENSE))
- © LIGHTest Consortium
