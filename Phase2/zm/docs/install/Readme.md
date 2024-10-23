# Deployment of the TDZM Component:

Deployment is done via HELM charts in a kubernetes cluster of your choice. Alternatively you can run a local instance through docker-compose. Due to the inclusion of the NSD DNS Server it is not recommended to run the backend component natively.

## Running TDZM via Docker-compose

Go into the `deploy/local` directory and run the `deploy.sh` script. You can specify an optional argument `build` which will run the docker build steps before starting the components. e.g.: `./deploy.sh build`

The compose file will startup a keycloak instance, the tdzm backend and the tdzm ui all together.

Configuration settings are already set as necessary to work, and should not be changed.

The Backend is reachable at: http://localhost:16001

The UI is reachable at: http://localhost:8001/ui

## Running TDZM Backend in Kubernetes Cluster

Deployment in Kubernetes is done via separate Helm charts.

## Scaling Zone Manager:
The Zone manager requires a shared volume, that can be mounted by all pods. Currently the volume is defined as "ReadWriteMany", but the Cluster you have must properly support this option. It could be done via NFS File System Storage classes, but may vary from provider to provider. Alternatively you can setup pure NSD Server instances, and connect them as Secondary servers through the Configuration options.

### General Prerequisites:
When you want to operate the TDZM Component you MUST ensure the following is available:

- NS Record specifying the TDZM as Nameserver for your desired zone. Example: desired Zone: trust.federation1.com -> Nameserver: ns1.trust.federation1.com
- A Record resolving your Nameserver to the IP Address the TDZM will be hosted.

These Records must be configured in the origin zone, so for your Trust-Domain `trust.federation1.com` you must configure them in the DNS Server for `federation1.com`

You will need to specify your chosen NS Name in the TDZM Configuration, see below.

### Cluster Requirements:
- Nginx as Ingress Controller
- Availability of TLS Certificates as secret in the deployment namespace.
- **OR** Letsencrypt Cert issuer setup for nginx ingress

### Charts for TDZM Backend & DNS Server

See [Helm Charts](../../deploy//helm/nsd/) for the TDZM Backend charts.

Configuration of the Deployment is done via the Helm values file. A Reference of available Configuration Options is visible below (all keys are part of "application.properties"):

| Property                                       | Description                                    | Default Value                                  |
| ---------------------------------------------- | ---------------------------------------------- | ---------------------------------------------- |
| `zoneConfig.TF_DOMAIN_NAME` | Specifies which DNS Domain this Zone Manager is responsible for. Our Demo deployment uses `trust.train1.xfsc.dev` | `your_federation_zone` |
| `zoneConfig.TF_DOMAIN_IP` | IP Address that your zone should be managed under | `1.2.3.4` |
| `zoneConfig.PRIMARY_SERVER_NSD` | Domain name of your primary dns server | `nameserver1_for_your_federation_zone` |
| `zoneConfig.PRIMARY_SERVER_IP` | Public IP Address of your primary dns server | `1.2.3.4` |
| `zoneConfig.SECONDARY_SERVER_1_NSD` | Domain name of your secondary dns server  | `nameserver2_for_your_federation_zone` |
| `zoneConfig.SECONDARY_SERVER_1_IP` | Public IP Address of your secondary dns server| `1.2.3.4` |
| `zoneConfig.SECONDARY_SERVER_2_NSD` | Domain name of your tertiary dns server  | `nameserver3_for_your_federation_zone` |
| `zoneConfig.SECONDARY_SERVER_2_IP` | Public IP Address of your tertiary dns server | `1.2.3.4` |
| `authConfigFileContent.ISSUER_URL` | Specified which Oauth Server is allowed. TZDM will check the JWT signature against this issuer. | `<your issuer>` |
| `authConfigFileContent.CLIENT_ID` | Specified which ClientID /audience should be allowed in a token. If a token does not contain this audience it will be considered Unauthorized. | `<your allowed client/audience>` |
| `authConfigFileContent.ALLOW_UNSAFE_SSL` | Specify whether you want to check the SSL certs of your configured issuer. **Setting this to "true" has security implications!** | `false` |

## Running TDZM UI in Kubernetes Cluster

Deployment in Kubernetes is done via separate Helm Chart

### Cluster Requirements:
- Nginx as Ingress Controller
- Availability of TLS Certificates as secret in the deployment namespace.
- **OR** Letsencrypt Cert issuer setup for nginx ingress


### Charts for TDZM UI

See [Helm Charts](../../deploy//helm/ui/) for the TDZM UI charts.

Configuration of the Deployment is done via the Helm values file. A Reference of available Configuration Options is visible below (all keys are part of "application.properties"):

| Property                                       | Description                                    | Default Value                                  |
| ---------------------------------------------- | ---------------------------------------------- | ---------------------------------------------- |
| `zonemgr_url` | Defines where the TDZM Backend is reachable. | `<zonemgr URL>` |
| `oidc_issuer_url` | Which issuer to use to get a token | `<oidc_issuer_url>` |
| `oidc_client_id` | Which client to use to connect tot he issuer | `your client id` |
| `oidc_client_secret` | Which secret to use with the client id | `your client secret` |
| `oidc_scopes` | Scopes to use when requesting a token | `openid email profile` |
| `ui_host` | What the hostname is where this deployment is reachable | `your ui host` |
| `cookie_secret` | A cookie secret to be used when storing the auth token cookie | `a cookie secret` |
| `node_tls_reject_unauthorized` | Whether to verify SSL certs when connecting to issuer, or TDZM Backend. Set to "0" to allow unsafe certs. | `1` |
| `app_base_url_path` | Which url prefix this deployment is reachable under. Currently only "/ui" is supported! **DO NOT CHANGE** | `/ui` |
