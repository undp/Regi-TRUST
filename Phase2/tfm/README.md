# TFM -Train Trust Framework Manager for RegiTRUST Phase 2

## Description

Train Trust Framework Manager - TFM, formerly named TSPA (Trust Service Publication Authority) is the [TRAIN](#about-train) component that allows Trust Framework and Trust List Provision and management. The current repository is the specific implementation for the RegiTRUST project in its second phase.

TFM is responsible for configuring and managing trust frameworks with its corresponding trust lists. It allows creation of trust lists for corresponding trust framework names. TFM also helps to anchor URIs (pointers to trust lists) and trust frameworks in the DNS system by connecting to the [DNS Zone Manager](/Phase2/zm/) component of the TRAIN infrastructure. TFM also provides a set of REST API endpoints responsible for onboarding entities into trust list.

## Requirements

- Java 21 or newer
- Maven ~3.9.8
- MongoDB 8.0.3 or newer
- Tomcat 10.1.16 or newer
- Running instance of Zone Manager
- Running instance of MongoDB
- Fully qualified domain name (FQDN)
- See [pom.xml](./pom.xml) for detailed dependencies and versions.

## Documentation

#### - [Installation and configuration guide](./doc/Installation_guide.md)

#### - [REST API](./doc/TSPA_openapi.yaml)

#### - [Postman collection](./doc/TFM_RegiTRUST_Phase2.postman_collection.json)

## About TRAIN

TRAIN provides components for a flexible and cross-domain trust infrastructure to sovereignly manage trust anchors with DNS(SEC) and verify the inclusion of entities (e.g. issuers of self-sovereign identity credentials) in trust frameworks.

For more information please visit the website: https://www.hci.iao.fraunhofer.de/de/identity-management/identity-und-accessmanagement/TRAIN_EN.html

## Authors and acknowledgment

Current project implementation by Fraunhofer IAO:

- Juan Vargas [Gitlab](https://gitlab.eclipse.org/juanvargas)/[Github](https://github.com/jcamilov)
- Isaac Henderson [Gitlab](https://gitlab.eclipse.org/isaachenderson)/[Github](https://github.com/hendersonweb)

With the support and or contributions of:

- [Abdul Farooqui](https://github.com/abdulFarooqui)
- [John Walker](https://github.com/jtwalker2000)

# License

- Apache License 2.0 (see [LICENSE](./LICENSE))
