## Installation & Configuration Guide

The application supports three different ways to run it:

- Local run with `maven`. See detailed instructions in this [document](../../deploy/local/README.md#how-to-build-the-project-locally). Configured with [application.yml](../../src/main/resources/application.yml)
- Run TSPA image in `docker`. See docker-related details in this [readme](../../deploy/local/README.md#how-to-run-the-docker-image-published-on-harbour-registry).
- Deploy and run TSPA service in `kubernetes` environment. See the [Helm charts](../../deploy/helm/README.md) for TSPA service itself and for IPFS and Keycloak Deployments. Application deployment configuration is managed through the Helm [values file](../../deploy/helm/tspa-service/values.yaml)
- TSPA uses [shared repository]("https://gitlab.eclipse.org/eclipse/xfsc/train/train-shared") for xml & json (de-)serialization which is integrated in TSPA as submodules.
- The TSPA also provides a UI Component (optional) which can be used to anchor trust frameworks and DIDs in the DNS. Currently the access of UI is secured via OIDC JWT Tokens. Setting up of TSPA UI locally can be found in this [tspa-ui-readme](../../ui/README.md).  See the [Helm charts](../../deploy/helm/README.md) for TSPA UI service . Helm deployment configuration is managed through the [values file](../../deploy/helm/ui/values.yaml) file.

The most important TSPA configuration settings are specified below: 

| Property                             | Description                                      | Default value                                                 |
|--------------------------------------|:-------------------------------------------------|:--------------------------------------------------------------|
| resolverdriver.didmethods            | DID Methods accepted by TSPA for Trust Framework Enrollment| -  `did:web:` |
| tspa.ipfs.rcp.api                    | IPFS Endpoint with IP of the machine where IPFS is running | /ip4/{your.ipv4}/tcp/5001  |
| server.port                    |  | '16003' |
| logging.level.root                    |  | INFO |
| logging.level.eu.xfsc.train.tspa                    |  | DEBUG |
| zonemanager.Address                    | Zone Manager Domain Name Configuration | 'https://testtrain.trust-scheme.de' |
| zonemanager.token-server-url                    | Auth backend between TSPA and ZM configuration  | 'https://essif.iao.fraunhofer.de/auth/realms/gxfs-dev-test/protocol/openid-connect/token' |
| zonemanager.grand-type                    | Auth backend between TSPA and ZM Grant type | 'client_credentials' |
| zonemanager.client-id                    | Auth backend between TSPA and ZM client-id | 'xfsctest' |
| zonemanager.client-secret                    | Auth backend between TSPA and ZM client-secret  | 'hidden-value' |
| zonemanager.query.status                     |  | true |
| trustlist.vc.hashAlgo                    | Select algorithm for creating hash of the trustlist. | Sha2-256 |
| trustlist.vc.issuer                    | issuer of vc trust list | did:web:essif.iao.fraunhofer.de |
| trustlist.vc.proof.signaturesuit                    | signature suite for creating vc trustlist. Select the Signature Suites based on the signature algorithem for proof of the trustlist configuration. List of Signature Suites:{"RsaSignature2018", "Ed25519Signature2018", "Ed25519Signature2020", "JcsEd25519Signature2020", "EcdsaSecp256k1Signature2019",! "EcdsaKoblitzSignature2016", "JcsEcdsaSecp256k1Signature2019", "BbsBlsSignature2020", "JsonWebSignature2020"} | JsonWebSignature2020 |
| trustlist.vc.jwt.kid                    |  | did:web:essif.iao.fraunhofer.de#yaHbNw6nj4Pn3nGPHyyTqP-QHXYNJIpkA37PrIOND4c |
| trutlist.vc.signature.algo                    | Select algorithem for creating VC. supported algo: {EdDSA, RS256, PS256, ES256K, BBSPlus, ES256, ES384, ES512, "ES256KCC","ES256KRR" } NOTE: Please select Algorithm based on the Private-key that loaded in the "/tspa/src/main/resources/Vault/VcJWKPrivateKey". | EdDSA |
| trustlist.vc.signer.type                    | Able to internal VC signer using either "INTERNAL" or "TSA"  | TSA |
| trustlist.vc.signer.url                    | configure TSA Signer as external VC Signer  | for local/docker use -> "https://zonemgr.train1.xfsc.dev/signer/v1/credential"  for k8s helm use -> "http://signer.signer.svc.cluster.local:8080"|
| trustlist.vc.signer.key                    | key name of the TSA or internal VC  | test |
| trustlist.vc.signer.namespace                    | Name space config of TSA  | signer |
| trustlist.vc.signer.group                    | Group config of TSA  | " " |
| well-known.credentialSubject.origin                      | | https://essif.iao.fraunhofer.de |
| well-known.issuer                      | | did:web:essif.iao.fraunhofer.de |
| well-known.jwt.kid                      | | did:web:essif.iao.fraunhofer.de#yaHbNw6nj4Pn3nGPHyyTqP-QHXYNJIpkA37PrIOND4c |
| well-known.signature.algo                    |     Select algorithem for creating Well-known configuration. supported algo: {EdDSA, RS256, PS256, ES256K, BBSPlus, ES256, ES384, ES512, "ES256KCC","ES256KRR" }NOTE: Please select Algorithm based on the Private-key that loaded in the "/tspa/src/main/resources/Vault/VcJWKPrivateKey".  | EdDSA |
| well-known.proof.signaturesuit                       | List of Signature Suites:{"RsaSignature2018", "Ed25519Signature2018", "Ed25519Signature2020", "JcsEd25519Signature2020", "EcdsaSecp256k1Signature2019",! "EcdsaKoblitzSignature2016", "JcsEcdsaSecp256k1Signature2019", "BbsBlsSignature2020", "JsonWebSignature2020"} | JsonWebSignature2020 |
| storage.type.trustlist           | Choose the storage type for trustlists. Thus, choose between "INTERNAL" and "IPFS" for the store location.|INTERNAL                                        |
| storage.path.vc           | | /tmp/train-tspa/store/VC/                                        |
|storage.path.well-known           | | /tmp/train-tspa/well-known/                  |
| storage.path.scheme           | | /tmp/train-tspa/store/schemes/  |
| storage.path.did           | | /tmp/train-tspa/store/DID_URI/   |
| storage.path.trustlist           | |/tmp/train-tspa/store/trust-lists/ |
| request.get.mapping           | Static url with name of the Server,where TSPA running. For example - "http://{name server of TSPA}/tspa-service/tspa/v1/" | http://localhost:16003/tspa-service/tspa/v1/                             |

## Notes
The Internal Storage Type is strongly discouraged for production use!

If you want to use **more** than 1 Replica, then you **MUST** Configure the component to use the IPFS storage, so that there are no lost update problems!

The Docker instance of "Keycloak" (keycloak-idm-server) in Docker-compose is only for testing UI login. Inorder to test JWT validation it is advisable to use keycloak or OIDC servers with https.
