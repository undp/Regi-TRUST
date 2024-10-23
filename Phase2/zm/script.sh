#! /bin/bash
#
# Setup script. It will take the configuration file and will initialize an intermediate sql DB, 
# fill it with initial records for the creation of zone file. It also configures and sets the nsd
# server up and starts the Zone Manager server.
#
# Must be called as ./script.sh $PORT
# The Zone Manager server will start at $PORT number.
set -eu -o pipefail

RUNNER=${RUNNER:-dns-zone-manager-server}
echo "[INFO] ====>>> STARTING ZONE MANAGER SERVICE SETUP"

echo "[INFO] Setting up NSD (default configuration)..."
# Configure NSD server
cat > /tmp/server.conf <<EOF
server:
    ip-address: 0.0.0.0
    ip-address: ::0
    verbosity: 3
    pidfile: "/var/run/nsd/nsd.pid"
    #port: 5353
EOF
mv /tmp/server.conf /etc/nsd/nsd.conf.d/server.conf

cat > /tmp/remote.conf << EOF
remote-control:
    control-enable: yes
    control-interface: 0.0.0.0
    control-port: 8952
    server-key-file: "/etc/nsd/nsd_server.key"
    server-cert-file: "/etc/nsd/nsd_server.pem"
    control-key-file: "/etc/nsd/nsd_control.key"
    control-cert-file: "/etc/nsd/nsd_control.pem"
EOF
mv /tmp/remote.conf /etc/nsd/nsd.conf.d/remote.conf

service nsd start
echo
service nsd status

printf "\nGetting DNS configuration variables from config file... \n"
# Get configuration variables from the config file
CONFIG_FILE="config.conf"
if [ -f "$CONFIG_FILE" ]; then
    # shellcheck source=config.conf
    source "${CONFIG_FILE}"
    echo "[INFO] Config file found. Trust framework domain name to set: $TF_DOMAIN_NAME"
else
    echo "Using Env Variables"
fi

ZM_PATH="/usr/lib/zonemgr/"		# path for source code.
VAR_PATH="/var/lib/zonemgr/"	# path for variables including DB, zonefile and DNS config file
SQLITE_DB="sqlite:///${VAR_PATH}zones.db"
SERVER_PORT=${1}

# skip DB configuration in case of ZM reload (already existing DB in persistent storage volume)
if [ ! -f "${VAR_PATH}zones.db" ]; then
  echo "[INFO] Initializing intermediate DB and adding records to it and to the zone file..."
  echo "[INFO] NSD server will be reload and reconfigured several times."
  echo "[INFO] This will print 'ok' and 'reconfig start' messages repeatedly:"
  echo

  # initialize DB
  ${RUNNER} --database "$SQLITE_DB" init

  # add default environment
  ${RUNNER} \
    --database $SQLITE_DB \
    add-environment \
    --environment network \
    --nsd-name "${PRIMARY_SERVER_NSD}" \
    --nsd-conf "$VAR_PATH"nsd.zones.conf \
    --nsd-reload "$ZM_PATH"reload-nsd.sh \
    --key-file "$VAR_PATH"private_key.tmp

  # add default zone with given trust framework domain
  ${RUNNER} \
    --database $SQLITE_DB \
    add-zone \
    --environment network \
    --apex "${TF_DOMAIN_NAME}" \
    --pattern network

  # add required NS records and related IP addresses
  ${RUNNER} \
    --database $SQLITE_DB \
    add-record \
      --environment network \
      --apex "${TF_DOMAIN_NAME}" \
      "$TF_DOMAIN_NAME" NS \
        "$PRIMARY_SERVER_NSD" \
        "$SECONDARY_SERVER_1_NSD"

  ${RUNNER} \
    --database $SQLITE_DB \
    add-record \
      --environment network \
      --apex "$TF_DOMAIN_NAME" \
        "$TF_DOMAIN_NAME" A "$TF_DOMAIN_IP"

  ${RUNNER} \
    --database $SQLITE_DB \
    add-record \
      --environment network \
      --apex "$TF_DOMAIN_NAME" \
        "$PRIMARY_SERVER_NSD" A "$PRIMARY_SERVER_IP"

  ${RUNNER} \
    --database $SQLITE_DB \
    add-record \
      --environment network \
      --apex "$TF_DOMAIN_NAME" \
        "$SECONDARY_SERVER_1_NSD" A "$SECONDARY_SERVER_1_IP"

  ${RUNNER} \
    --database $SQLITE_DB \
    add-record \
      --environment network \
      --apex "$TF_DOMAIN_NAME" \
        "$SECONDARY_SERVER_2_NSD" A "$SECONDARY_SERVER_2_IP"

  echo
  echo "[INFO] Adding cron job for resigning..."

  # Install the DNSSEC resigning cronjob:
  crontab -u zonemgr "$ZM_PATH"etc/crontab

else
  echo "A zone DB file was found. Zone manager reload"
fi

echo
echo "[INFO] Configuring NSD for ZM..."

cat > /tmp/zonemgr.conf <<EOF
pattern:
    name: network
    notify: $SECONDARY_SERVER_1_IP NOKEY
    #notify: $SECONDARY_SERVER_2_IP NOKEY
    provide-xfr: $SECONDARY_SERVER_1_IP NOKEY
    #provide-xfr: $SECONDARY_SERVER_2_IP NOKEY

include: /var/lib/zonemgr/nsd.zones.conf
EOF
mv /tmp/zonemgr.conf /etc/nsd/nsd.conf.d/zonemgr.conf

service nsd start
echo
service nsd status

echo
echo "enforcing NSD reconfiguration..."
nsd-control reconfig

echo
echo "[INFO] Starting the Zone Manager service in port ${SERVER_PORT} ..."
${RUNNER} --database $SQLITE_DB server -e network "0.0.0.0:${SERVER_PORT}" ${RUN_WITH_WSGI:-} ${GENERATE_SWAGGER:-}
