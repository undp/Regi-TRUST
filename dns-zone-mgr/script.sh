#! /bin/bash
#
# A simple script to load the database.

ZMGR=./zonemanager.py
WORKDIR=test
ADDR=127.0.0.1:8088
BASE=http://$ADDR

api() {
	curl -X $1 -H "Authorization: Bearer $TOKEN" http://$ADDR$2
}

vapi() {
	curl -v -X $1 -H "Authorization: Bearer $TOKEN" http://$ADDR$2
}

api_put() {
	curl -H "Authorization: Bearer $TOKEN" \
		-H 'Content-Type: application/json' \
		-X PUT \
		--data-raw "$2" \
		http://$ADDR$1
}

mkdir -p $WORKDIR
$ZMGR init -f
$ZMGR add-environment -e default -n ns.lightest.nlnetlabs.nl. \
	-r "echo foo" \
	-c $WORKDIR/nsd.zones.conf -k $WORKDIR/private_key.tmp
$ZMGR add-zone -e default -a lightest.nlnetlabs.nl.
$ZMGR add-record -e default -a lightest.nlnetlabs.nl. \
	lightest.nlnetlabs.nl. A 127.0.0.1 127.0.0.2
TOKEN=`$ZMGR create-token -e default default lightest.nlnetlabs.nl.`
coproc $ZMGR server -e default
sleep 1
api GET /status
api_put /names/scheme..lightest.nlnetlabs.nl./trust-list \
	'{"url":"http://lightest.nletlabs.nl/","certificate":[{"data":"1234567890123456789012345678901234567890123456789012345678901234"}]}'
while true ; do
api_put /names/scheme.lightest.nlnetlabs.nl./trust-list \
	'{"url":"http://lightest.nletlabs.nl/","certificate":[{"data":"1234567890123456789012345678901234567890123456789012345678901234"}]}'
done
echo PUT /names/scheme.lightest.nlnetlabs.nl./trust-list broken
api_put /names/scheme.lightest.nlnetlabs.nl./trust-list \
	'{"url":"http://lightest.nletlabs.nl/","certificate":[{"data":""}]}'
echo GET /names/scheme.lightest.nlnetlabs.nl./trust-list
api GET /names/scheme.lightest.nlnetlabs.nl./trust-list
echo
api DELETE /names/scheme.lightest.nlnetlabs.nl./trust-list
api_put /names/scheme.lightest.nlnetlabs.nl./trust-list \
	'{"url":"http://lightest.nletlabs.nl/","certificate":[{"data":"1234567890123456789012345678901234567890123456789012345678901234"}]}'
$ZMGR resign
api_put /names/trustservice.lightest.nlnetlabs.nl./schemes \
	'{"schemes": [ "some.scheme.com", "sööome.other.scheme.com" ] }'
api_put /names/trustservice.lightest.nlnetlabs.nl./schemes \
	'{"schemes": [ "some.scheme.com", "some.other.scheme.com" ] }'
api GET /names/trustservice.lightest.nlnetlabs.nl./schemes
api DELETE /names/trustservice.lightest.nlnetlabs.nl./schemes 
api DELETE /names/trustservice.lightest.nlnetlabs.nl./schemes 
api GET /names/trustservice.lightest.nlnetlabs.nl./schemes
api_put /names/trustservice.lightest.nlnetlabs.nl./schemes \
	'{"schemes": [ "some.scheme.com", "some.other.scheme.com" ] }'

kill $COPROC_PID
sleep 1
