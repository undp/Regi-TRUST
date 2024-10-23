# Integration with TSA

TFM should use the TSA to sign the trust list VCs. TSA can be configured in the application.yaml and helm charts.
The TSA Deployment is **not** detailed in the TRAIN Documentation, as the TSA setup instructions could be changed with new releases and then our documentation would not work anymore.

**Current TSA Signer version is not compatible with TCR VC verification procedure so examplary deployments of the TFM use the internal signer implementation.**

## Configuration in application.yaml & Helm Charts

```
signer:
      type: "TSA"
      url: "https://zonemgr.train1.xfsc.dev/signer/v1/credential"
      key: "test"
      namespace: "signer"
      group: ""
```

**_url_** points to the public instance of the TSA deployed. **_key_** is the keyname configured in TSA Vault. **_namespace_** is also initialized in TSA.

The deployment details of TSA can be found [here](https://gitlab.eclipse.org/eclipse/xfsc/tsa/signer). TSA is used only for signing in TFM and not used for validation purposes.
