# Integration with OCM and PCM

Based on the integration requirements we provide documentation regarding usage of the TRAIN Framework for OCM and PCM components.

## Setup
For a Setup of the Train Trust Framework Manager only, you should follow this components local build guide found here: [Local Build](./../install/README.md)

If you want to setup the broader Train Framework locally, follow the local Train setup guide here: [Local Train Framework Setup](https://gitlab.eclipse.org/eclipse/xfsc/train/TRAIN-Documentation/-/tree/main/demonstration/local?ref_type=heads)

To setup a production grade environment look at the production grade setup guide here: [Production Train Framework Setup](https://gitlab.eclipse.org/eclipse/xfsc/train/TRAIN-Documentation/-/tree/main/demonstration?ref_type=heads)

## General Usage
Since Train is not using the OCM/PCM as a Service or Dependency there is no special configuration needed on the Train components side. For OCM/PCM Users general usage of Train follows the general usage as for any other user.

There are Postman collections showcasing the usage of each Train component from Creating a Trust List, Publishing TSPs, and even verifying a VC for both, local and production grade full Train setups.
Instructions how to use and import the collections can be found in the setup manuals linked above.

### Local Build usage:
If you want to have a general simple way of interacting solely with the Train Trust Framework Manager here are the basic steps:
1. Make sure to initialize the trust list either in json/xml formats based on [TrustList Setup Documentation](./../operation/TrustListSetup.md)
2. Now once the trust list is setup successfully you can use the corresponding [TSP CRUD Operation Documentation](./../operation/TSP-CRUD-Operations.md) to enroll new issuers and authorities in the trust-list. The details regarding Trust Lists & TSP Data model can be found in the following [link](./../TrustList_DataModel_Design/templates-trustlist/)
3.  Then make sure that you anchor the trust framework name and DID in the DNS using the following [documentation](./../operation/TrustFrameworkSetup.md)
4. TSPA Service is secured by JWT Token. So make sure you configure the the right OAuth2.0/OIDC Server for authorization&authentication purpose. The example realm used for keycloak can be found [here](./../../keycloak/realm-export.json).