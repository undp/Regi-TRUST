# see https://makefiletutorial.com/

SHELL := /bin/bash -eu -o pipefail

PYTHON_3 ?= python3
PYTHON_D ?= $(HOME)/.python.d
SOURCE_PATHS := "zonedb"
CMD_SELENIUM := train_bdd.selenium # :: NOT Implemented yet

VENV_PATH_DEV := $(PYTHON_D)/dev/eclipse/xfsc/train/dns-zone-manager-server
VENV_PATH_PROD := $(PYTHON_D)/prod/eclipse/xfsc/train/dns-zone-manager-server

setup_dev: $(VENV_PATH_DEV)
	mkdir -p .tmp/

$(VENV_PATH_DEV):
	"$(PYTHON_3)" -m venv "$(VENV_PATH_DEV)"
	"$(VENV_PATH_DEV)/bin/pip" install -U pip wheel
	"$(VENV_PATH_DEV)/bin/pip" install -e ".[dev]"

setup_prod: $(VENV_PATH_PROD)

$(VENV_PATH_PROD):
	"$(PYTHON_3)" -m venv $(VENV_PATH_PROD)
	"$(VENV_PATH_PROD)/bin/pip" install -U pip wheel
	"$(VENV_PATH_PROD)/bin/pip" install .

isort: setup_dev
	"$(VENV_PATH_DEV)/bin/isort" $(SOURCE_PATHS) tests

pylint: setup_dev
	"$(VENV_PATH_DEV)/bin/pylint" $${ARG_PYLINT_JUNIT:-} $(SOURCE_PATHS) tests

coverage_run: setup_dev
	"$(VENV_PATH_DEV)/bin/coverage" run -m pytest $${ARG_COVERAGE_PYTEST:-} -m "not integration" tests/ src/

coverage_report: setup_dev
	"$(VENV_PATH_DEV)/bin/coverage" report

mypy: setup_dev
	"$(VENV_PATH_DEV)/bin/mypy" $${ARG_MYPY_SOURCE_XML:-} $(SOURCE_PATHS)

code_check: \
	setup_dev \
	isort \
	pylint \
	coverage_run coverage_report \
	mypy

run_all_test_coverage: coverage_run coverage_report

clean_dev:
	rm -rfv "$(VENV_PATH_DEV)"

clean_prod:
	rm -rfv "$(VENV_PATH_PROD)"

activate_env_prod:
	@echo "source \"$(VENV_PATH_PROD)/bin/activate\""

activate_env_dev:
	@echo "source \"$(VENV_PATH_DEV)/bin/activate\""

licensecheck: setup_dev
	"$(VENV_PATH_DEV)/bin/pip" freeze > ".tmp/requirements.txt"
	cd .tmp/ && "$(VENV_PATH_DEV)/bin/licensecheck" -u requirements > THIRD-PARTY.txt

coverage_table:
	docker run --rm \
          -v .:/usr/lib/zonemgr \
          -v ./var/lib/zonemgr:/var/lib/zonemgr/ \
          train/dev-dns-zone-manager \
          coverage report

coverage_html:
	docker run --rm \
          -v .:/usr/lib/zonemgr \
          -v ./var/lib/zonemgr:/var/lib/zonemgr/ \
          train/dev-dns-zone-manager \
          coverage html

docker-build-dns-zone-manager-server:
	docker build -t localhost/dns-zone-manager-server .

docker-build-dns-zone-manager-server-dev: docker-build-dns-zone-manager-server
	docker build -t localhost/dns-zone-manager-server-dev -f Dockerfile.dev .

docker-run-dns-zone-manager-server:
	docker run -p 16001:16001 -p 53:53 -p 53:53/udp localhost/dns-zone-manager-server

docker-run-dns-zone-manager-server-dev:
	mkdir -p "$(CURDIR)/.tmp"
	mkdir -p "$(CURDIR)/persistence"
	docker run -it \
		-p 16001:16001 -p 53:53 -p 53:53/udp \
		-v "$(CURDIR)/.tmp:/mounted/.tmp" \
		-v "$(CURDIR)/zonedb:/usr/lib/zonemgr/zonedb" \
		-v "$(CURDIR)/persistence:/var/lib/zonemgr" \
		localhost/dns-zone-manager-server-dev 

docker-pytest-dns-zone-manager-server-dev:
	mkdir -p "$(CURDIR)/.tmp"
	docker run \
		-v "$(CURDIR)/.tmp:/mounted/.tmp" \
		-v "$(CURDIR)/tests:/mounted/tests" \
		-e DNS_ZONE_MANAGER_SERVER_AUTH_CONF_PATH='auth.conf' \
		--network=host \
		localhost/dns-zone-manager-server-dev \
		pytest /mounted/tests -v

docker-bash-dns-zone-manager-server-dev:
	mkdir -p "$(CURDIR)/.tmp"
	docker run -it \
		-v "$(CURDIR)/.tmp:/mounted/.tmp" \
		-v "$(CURDIR)/tests:/mounted/tests" \
		-e DNS_ZONE_MANAGER_SERVER_AUTH_CONF_PATH='auth.conf' \
		--network=host \
		localhost/dns-zone-manager-server-dev \
		bash

docker-coverage-txt-dns-zone-manager-server-dev:
	mkdir -p "$(CURDIR)/.tmp"
	docker run -it \
		-v "$(CURDIR)/.tmp:/mounted/.tmp" \
		-v "$(CURDIR)/tests:/mounted/tests" \
		-e DNS_ZONE_MANAGER_SERVER_AUTH_CONF_PATH='auth.conf' \
		--network=host \
		localhost/dns-zone-manager-server-dev \
		coverage report --data-file=/mounted/.tmp/.coverage

docker-coverage-html-dns-zone-manager-server-dev:
	mkdir -p "$(CURDIR)/.tmp"
	docker run -it \
		-v "$(CURDIR)/.tmp:/mounted/.tmp" \
		-v "$(CURDIR)/tests:/mounted/tests" \
		-e DNS_ZONE_MANAGER_SERVER_AUTH_CONF_PATH='auth.conf' \
		--network=host \
		localhost/dns-zone-manager-server-dev \
		coverage html --data-file=/mounted/.tmp/.coverage --directory=/mounted/.tmp/htmlcov


