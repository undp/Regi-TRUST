## Helm charts

Here we provide helm charts for [TSPA Service](./tspa-service)

First clone the TRAIN Trust Framework Manager (TFM) repository:

For manual TFM installation with helm charts go to the `helm` folder and run standard helm commands:

```bash
helm > helm install tspa-svc ./tspa-service
```

TSPA UI is optional and TSPA Service can work independently. For manual TSPA UI installation with helm charts go to the `helm` folder and run standard helm commands:

```bash
helm > helm install tspa-ui-svc ./ui
```

We also publish prepared charts into Helm repository as part of CICD pipeline, so the charts can be used in overall cluster deployment with tools like `ArgoCD`
