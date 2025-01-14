## Zone Manager Configuration properties

The Zone Manager is configured via 2 configuration files:

For the **Zone Manager server related configuration**, see an examplary file [here](../../auth.sample.conf).

The following table contains a description of server related properties:

| Property                 | Description                                                                                                       | Value                                  |
| ------------------------ | ----------------------------------------------------------------------------------------------------------------- | -------------------------------------- |
| `TF_DOMAIN_NAME`         | Specifies which DNS Domain this Zone Manager is responsible for. Our Demo deployment uses `trust.train1.xfsc.dev` | `your_federation_zone`                 |
| `TF_DOMAIN_IP`           | IP Address that your zone should be managed under                                                                 | `1.2.3.4`                              |
| `PRIMARY_SERVER_NSD`     | Domain name of your primary dns server                                                                            | `nameserver1_for_your_federation_zone` |
| `PRIMARY_SERVER_IP`      | Public IP Address of your primary dns server                                                                      | `1.2.3.4`                              |
| `SECONDARY_SERVER_1_NSD` | Domain name of your secondary dns server                                                                          | `nameserver2_for_your_federation_zone` |
| `SECONDARY_SERVER_1_IP`  | Public IP Address of your secondary dns server                                                                    | `1.2.3.4`                              |
| `SECONDARY_SERVER_2_NSD` | Domain name of your tertiary dns server                                                                           | `nameserver3_for_your_federation_zone` |
| `SECONDARY_SERVER_2_IP`  | Public IP Address of your tertiary dns server                                                                     | `1.2.3.4`                              |

For the **Oauth configuration**, see an examplary file [here](../../auth.sample.conf).

| Property           | Description                                                                                                                                    | Value                                                   |
| ------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------- |
| `ISSUER_URL`       | Specified which Oauth Server is allowed. TZDM will check the JWT signature against this issuer.                                                | `https://<<yourdomain.com/auth>>/realms/<<your_realm>>` |
| `CLIENT_ID`        | Specified which ClientID /audience should be allowed in a token. If a token does not contain this audience it will be considered Unauthorized. | `<your allowed client/audience>`                        |
| `ALLOW_UNSAFE_SSL` | Specify whether you want to check the SSL certs of your configured issuer. **Setting this to "true" has security implications!**               | `false`                                                 |
