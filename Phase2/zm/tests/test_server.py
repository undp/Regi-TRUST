"""Unit testing for Zone Manager server (JWT secured endpoints)"""
import os
from pathlib import Path

import pytest
import requests


def _headers(access_token: str) -> dict[str, str]:
    return {
        'Content-Type': "application/json",
        'Authorization': f"Bearer {access_token}"
    }


TIMEOUT = 10
_PORT = os.environ.get('ZM_SERVER_PORT', '16001')
SERVER_URL = f'http://localhost:{_PORT}'

_BASE_DOMAIN = 'dev-idm.iao.fraunhofer.de'

TF_NAME1 = f"bw.{_BASE_DOMAIN}"
TF_NAME2 = f"ludwigsburg.{_BASE_DOMAIN}"
TF_NAME3 = f"herrenberg.{_BASE_DOMAIN}"


@pytest.fixture(scope='session', name='token')
def get_token() -> str:
    """Get token from keycloak"""
    if not os.getenv('DNS_ZONE_MANAGER_SERVER_AUTH_CONF_PATH'):
        config_file_path = Path(__file__).parent / '../auth.conf'
    else:
        config_file_path = Path(os.getenv('DNS_ZONE_MANAGER_SERVER_AUTH_CONF_PATH'))

    config = {}

    exec(config_file_path.read_text(), config)

    # get token from identity provider defined in auth.conf
    token_endpoint = (f"{config['KEYCLOAK_URL']}"
                      f"/realms/{config['REALM']}/protocol/openid-connect/token")

    data = {
        'username': config['TEST_PASSWORD'],
        'password': config['TEST_USER'],
        'grant_type': 'password',
        'client_secret': config['TEST_CLIENT_SECRET'],
        'scope': config['TEST_SCOPE'],
        'client_id': config['CLIENT_ID'],
    }
    response = requests.post(token_endpoint, data=data, timeout=10)
    yield response.json()["access_token"]


def test_01_get_token(token) -> None:
    """Testing - auth (get token from identity provider)..."""
    assert token


def test_02_status_get(token) -> None:
    """Testing - check status endpoint..."""
    response = requests.get(
        url=f"{SERVER_URL}/status",
        headers=_headers(token),
        timeout=TIMEOUT
    )
    assert response.json()["status"] == "OK"


def test_03_scheme_claims_put(token) -> None:
    """Testing - check PUT trust framework endpoint..."""
    data = {
        'schemes': [TF_NAME1, TF_NAME2, TF_NAME3]
    }
    response = requests.put(
        url=f"{SERVER_URL}/names/{TF_NAME1}/schemes",
        json=data,
        headers=_headers(token),
        timeout=TIMEOUT
    )
    assert response.status_code == 200


def test_04_trust_list_put(token) -> None:
    """Testing - check PUT trust list endpoint..."""
    data = {
        'did': "did:web:some-did-for-bw"
    }
    _url = f"{SERVER_URL}/names/{TF_NAME2}/trust-list"

    response = requests.put(
        url=_url,
        json=data,
        headers=_headers(token),
        timeout=10
    )

    assert response.status_code == 200


def test_05_view_zone_get(token) -> None:
    """Testing - check GET view-zone..."""
    response = requests.get(
        url=f"{SERVER_URL}/view-zone",
        headers=_headers(token),
        timeout=TIMEOUT
    )
    assert response.status_code == 200


def test_06_trust_list_delete(token) -> None:
    """Testing - check DELETE trust list endpoint..."""
    response = requests.delete(
        url=f"{SERVER_URL}/names/{TF_NAME2}/trust-list",
        headers=_headers(token),
        timeout=TIMEOUT
    )
    assert response.status_code == 204


def test_07_scheme_claims_delete(token) -> None:
    """Testing - check DELETE trust framework endpoint..."""
    response = requests.delete(
        url=f"{SERVER_URL}/names/{TF_NAME1}/schemes",
        headers=_headers(token),
        timeout=TIMEOUT
    )
    assert response.status_code == 204
