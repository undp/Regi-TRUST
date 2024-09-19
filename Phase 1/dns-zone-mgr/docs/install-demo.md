# Installation of Demonstration Environment

This memo documents the installation of the Zone Manager for the Regi-TRUST project, previously
named [GCCN - Global COVID Certificate Network](https://www.lfph.io/webinars/gccn-trust-registry-network-proof-of-concept-demonstration-and-qa/).

## Resources

The environment uses three AWS cloud instances (other cloud providers
are available) named as follows:

- primary: UNDP-Pilot-DNS-Host-1
- secondary-sfo: UNDP-Pilot-DNS-Host-2
- secondary-sgp: UNDP-Pilot-DNS-Host-3

The primary droplet will be running the Zone Manager itself
providing the HTTPS API. It also runs NSD as the primary name server for
the zones served by the environment. The other two droplets will run NSD
as the secondary name servers for those zones.

As of writing this document, the droplets had the following addresses:

| Droplet       | IPv4 address  |
| ------------- | ------------- |
| primary       | 3.220.105.79  |
| secondary-sfo | 34.193.98.5   |
| secondary-sgp | 18.215.94.157 |

We’ll use these addresses in the code snippets below.

The top domain used for the project is `clearreg.org` and the Zone Manager is
hosted under `network.clearreg.org`.

All droplets were created using Debian GNU/Linux 11 (bullseye) images.

## All Droplets

```
apt-get update
apt-get dist-upgrade
apt-get install nsd
```

## primary

### Install and Configure Zone Manager

We are installing the zone manager itself into `/usr/lib/zonemgr`. It’s
database and NSD configuration will be in `/var/lib/zonemgr/`.

Install sqlite3 if you have not done it. Then, install the zone manager and add user to the system:

```
apt-get install git gunicorn python-sqlalchemy python-falcon python-ldns \
  sqlite3
git clone git@https://gitlab.cc-asp.fraunhofer.de/essif_dev_internal/zone-manager.git /usr/lib/zonemgr
adduser --system --home /var/lib/zonemgr zonemgr
cp /usr/lib/zonemgr/etc/zonemgr.service /etc/systemd/system
```

While primarily a web service, Zone Manager comes with a number of
maintenance commands. For each command, it requires the URI to a database
given via the --database (or -d, for short) option. This needs to be
given before the actual command. The URI is in SQL Alchemy format which
isn’t entirely intuitive. For SQLite, if you provide an absolute file
name, you need to start with four (!) slashes, e.g.,
sqlite:////var/lib/zonemgr/zones.db.
Before we can start, we need to create and initialize the database and then add an environment called `network`:

```
sudo python3 /usr/lib/zonemgr/zonemanager.py --database sqlite:////var/lib/zonemgr/zones.db init

sudo python3 /usr/lib/zonemgr/zonemanager.py \
  --database sqlite:////var/lib/zonemgr/zones.db \
  add-environment \
   --environment network \
   --nsd-name ns1.network.clearreg.org \
   --nsd-conf /var/lib/zonemgr/nsd.zones.conf \
   --nsd-reload /usr/lib/zonemgr/reload-nsd.sh \
   --key-file /var/lib/zonemgr/private_key.tmp

```

Add the zone we are going to use, `network.clearreg.org`:

```
sudo python3 /usr/lib/zonemgr/zonemanager.py \
  --database sqlite:////var/lib/zonemgr/zones.db \
  add-zone \
    --environment network \
    --apex network.clearreg.org \
    --pattern network
```

The command will print the DS record for the zone. You will need that
record to configure delegation in the parent zone, in this case
`clearreg.org`.

Make everything owned by the `zonemgr` user it can access things later.

```
chown -R zonemgr: /var/lib/zonemgr
```

Add some necessary records to the zone:

```
sudo python3 zonemanager.py \
  --database sqlite:////var/lib/zonemgr/zones.db \
  add-record \
    --environment network \
    --apex network.clearreg.org \
    network.clearreg.org NS \
      ns1.network.clearreg.org ns2.network.clearreg.org ns3.network.clearreg.org

sudo python3 zonemanager.py \
  --database sqlite:////var/lib/zonemgr/zones.db \
  add-record \
    --environment network \
    --apex network.clearreg.org network.clearreg.org A 3.220.105.79

sudo python3 zonemanager.py \
  --database sqlite:////var/lib/zonemgr/zones.db \
  add-record \
    --environment network \
    --apex network.clearreg.org ns1.network.clearreg.org A 3.220.105.79

sudo python3 zonemanager.py \
  --database sqlite:////var/lib/zonemgr/zones.db \
  add-record \
    --environment network \
    --apex network.clearreg.org ns2.network.clearreg.org A 34.193.98.5

sudo python3 zonemanager.py \
  --database sqlite:////var/lib/zonemgr/zones.db \
  add-record \
    --environment network \
    --apex network.clearreg.org ns3.network.clearreg.org A 18.215.94.157
```

Install the DNSSEC resigning cronjob:

```
crontab -u zonemgr /usr/lib/zonemgr/etc/crontab
```

Enable and start the service:

```
systemctl enable zonemgr
systemctl start zonemgr
```

### Install Nginx and Acmetool

Get the packages:

```
apt-get install nginx acmetool
```

Start nginx.

```
systemctl enable nginx
systemctl start nginx
```

Bootstrap acmetool. Run

```
acmetool quickstart
```

and then select ’Let’s Encrypt (live)’ and ‘WEBROOT’. Enter
`/var/www/html/.well-known/acme-challenge` is the webroot path (NB: This
is _not_ the default presented). Accept the terms, insert an email address
if you want to and agree to the cronjob to be installed.

Request a certificate:

```
acmetool want network.clearreg.org
```

Now finalize Nginx configuration:

```
cat > /etc/nginx/sites-available/network.clearreg.org <<EOF
server {
    listen 80;
    listen [::]:80;
    server_name network.clearreg.org;
    root /var/name/html;
    location / {
        return 301 https://$host$request_uri;
    }
}
server {
    listen 443 ssl;
	  listen [::]:443 ssl;
	  server_name network.clearreg.org;
	  ssl_certificate /var/lib/acme/live/network.clearreg.org/fullchain;
	  ssl_certificate_key /var/lib/acme/live/network.clearreg.org/privkey;
	  root /var/www/html;
	  location / {
		    proxy_pass http://127.0.0.1:8008$request_uri;
	  }
}
EOF
ln -s ../sites-available/network.clearreg.org /etc/nginx/sites-enabled/network.clearreg.org
```

Reload nginx:

```
systemctl reload nginx
```
