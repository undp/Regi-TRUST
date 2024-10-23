[Local Build](#how-to-build-the-project-locally)
# How to Build the project Locally
```bash
    git clone --recurse-submodules https://gitlab.eclipse.org/eclipse/xfsc/train/tspa.git
    cd tspa/
    mvn clean install
    docker-compose -f docker-compose.yml pull
    docker-compose up -d --build # Build images before starting containers. 

```

On successful build using the above commands will start TSPA service on your localhost at port `16003`, to check service status open its health page in browser at `http://localhost:16003/tspa-service/actuator/health`'.

[Docker Run](#how-to-run-the-docker-image-published-on-harbour-registry)
# How to run the docker image published on Harbour Registry

```bash
    git clone https://gitlab.eclipse.org/eclipse/xfsc/train/tspa.git
    cd tspa/deploy/local
    docker-compose up -d --pull always"|"missing"|"never"
```
[Keycloak Usage](#How to use the keycloak instance available in the dockercompose file?)
# How to use the keycloak instance available in the dockercompose file?

1. On Successful Build of TFM keycloak starts in port 8080, which is planned to be used for testing TSPA UI locally.
2. The credentials used for TSPA UI login is _username_: **testuser**, _password_: **testuser**
3. Most of the TFM endpoints are secured by JWT Tokens except GET requests. The OIDC Issuers can be configured in the [application.yaml](../../src/main/resources/application.yml) under the attribute _spring.security.oauth2.resourceserver.jwt.issuer-uri_.
4. JWT Token validation requires https request. That's why in the current implementation [https://essif.iao.fraunhofer.de/auth/realms/gxfs-dev-test](https://essif.iao.fraunhofer.de/auth/realms/gxfs-dev-test) is used.
5. Similar to TFM endpoint protection, the communication between zonemanager and TFM is also protected by JWT Token. Ideally same OIDC issuer can be used. But inorder to accomodate flexible governance we give the flexibility for the user to configure seperate OIDC issuer under the attribute _zonemanager.token-server-url_.
6. The implementation is flexible to use other OIDC Issuers other than keycloak.
7. The local keycloak implementation uses the [realmfile](../../keycloak/realm-export.json).