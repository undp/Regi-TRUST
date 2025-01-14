## Installation & Configuration Guide

We suggest to provide a single linux instance for the TFM service. The main deplyoment steps are:

1. Clone the project repository
2. Set the required property variables in the [application.yml](../../src/main/resources/application.yml) file
3. Build the project
4. Run the TFM service
5. Set the proxy in the nginx server for remote access

### 1. Clone the project repository

First, create a folder for the project, for example /home/train/work:

```bash
mkdir /home/train/work
cd /home/train/work
```

Then, clone the repository into the folder:

```bash
git clone git@github.com:undp/Regi-TRUST.git
cd Regi-TRUST/Phase2/tfm/
```

### 2. Set property variables

The [application.yml](../../src/main/resources/application.yml) file is the place where you need to set the required property variables before deployment.

The main properties are:

| Property               | Description                                                                                                                     | Example                                        |
| ---------------------- | :------------------------------------------------------------------------------------------------------------------------------ | :--------------------------------------------- |
| server.port            | Port number where TFM server will be listening on.                                                                              | '16003' (Default value)                        |
| server.url             | Domain name where the TFM service will be running. This is needed to create pointer to the Trust List in the DNS.               | https://tfm.regitrust.axyom.co                 |
| storage.path.trustlist | Where the Trust List will be stored. It must also be used by the proxy configuration to allow allocation of the XML trust list. | /home/train/work/train-tspa/store/trust-lists/ |

Authentication and data base related properties:

| Property                                             | Description                                                                        | Example                                          |
| ---------------------------------------------------- | :--------------------------------------------------------------------------------- | :----------------------------------------------- |
| spring.security.oauth2.resourceserver.jwt.issuer-uri | Oauth2 issuer URL, used to validate the JWT token for the authenticated endpoints. | https://auth.regitrust.axyom.co/realms/RegiTRUST |
| spring.data.mongodb.host                             | MongoDB host URL, used to store the trust lists.                                   | 'localhost'                                      |
| spring.data.mongodb.port                             | MongoDB port number, used to store the trust lists.                                | '27017'                                          |

Additionally, the TFM will connect to the Zone Manager to persist pointer to the Trust List. Please make sure the Zone Manager is running before using the TFM service. The following properties are required:

| Property                     | Description                                        | Example                                                                        |
| ---------------------------- | :------------------------------------------------- | :----------------------------------------------------------------------------- |
| zonemanager.Address          | Base URL where the Zone Manager has been deployed. | https://zm.regitrust.axyom.co                                                  |
| zonemanager.token-server-url | Auth backend between TSPA and ZM configuration     | https://auth.regitrust.axyom.co/realms/RegiTRUST/protocol/openid-connect/token |
| zonemanager.grand-type       | Auth backend between TSPA and ZM Grant type        | 'password'                                                                     |
| zonemanager.client-id        | Auth backend between TSPA and ZM client-id         | 'RegiTRUST_Client'                                                             |
| zonemanager.client-secret    | Auth backend between TSPA and ZM client-secret     |                                                                                |

### 3. Build the project

```bash
cd ~/work/Regi-TRUST/Phase2/tfm/
mvn clean install -DskipTests
```

### 4. Run the TFM service

We suggest to run the TFM as a service. If you don't want to run it as service, simply run:

```bash
mvn spring-boot:run
```

For running it as a service, please create a unit file named tfm.service for systemd in /etc/system/system, this runs the tfm in spring-boot on port 8080.

```bash
sudo nano /etc/systemd/system/tfm.service
```

This file should have the following contents:

```
[Unit]
Description=TRAIN Backend
After=network.target

[Service]
User=train
Group=train
WorkingDirectory=/home/train/work/Regi-TRUST/Phase2/tfm/
ExecStart=/usr/bin/mvn spring-boot:run
#SuccessExitStatus=143
Restart=always
RestartSec=10
StandardOutput=file:/var/log/tfm/out.log
StandardError=file:/var/log/tfm/error.log

[Install]
WantedBy=multi-user.target

```

Then reload systemd daemon, start and enable the service:

```bash
sudo systemctl daemon-reload
sudo systemctl start tfm
sudo systemctl enable tfm
```

### 5. Set the proxy in the nginx server for remote access

Configure nginx to proxy the http requests from https to the tfm service. The configuration file should be located in /etc/nginx/site-available and named tfm.registrust.conf.

The content of the file are:

```
server {
         listen 80;
         listen [::]:80;
         server_name tfm.regitrust.axyom.co;
         return 301 https://$server_name;
}
server {
         listen 443 ssl ;
         listen [::]:443 ssl ;
         server_name tfm.regitrust.axyom.co;
         ssl_certificate /etc/letsencrypt/live/tfm.regitrust.axyom.co/fullchain.pem;
         ssl_certificate_key /etc/letsencrypt/live/tfm.regitrust.axyom.co/privkey.pem;
         access_log /var/log/nginx/access_tfm.log;
         error_log /var/log/nginx/error_tfm.log warn;
         location / {
             include proxy_params;
             proxy_pass http://localhost:8080;
         }
         location /ttfm {
             include proxy_params;
             proxy_pass http://localhost:16003/ttfm;
         }
         location /trust-list/ {
             alias /home/train/work/train-tspa/store/trust-lists/;
             autoindex_exact_size off;   # legible sizes
         }
}
```

Finally, restart the nginx service:

```bash
sudo systemctl restart nginx
```
