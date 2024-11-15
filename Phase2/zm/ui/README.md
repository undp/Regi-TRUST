# Running:
If running the first time you **must** run `npm install`

Then run `npm run dev` to start the development mode server and page.

# Developing

## Start development Keycloak
To develop the UI, there must be an OIDC Server available to enable a login on your local machine. We chose Keycloak as our OIDC server. To start it follow below steps:

```sh
cd dev_infra
docker compose up -d
```

The pre-defined user credentials are:
- username: "devuser"
- password: "devpassword"

## Start the UI:
Next you need to start the ui by running `npm run dev`

**Caveat: Since the keycloak is reachable from the browser as "localhost", the TDZM backend will not be able to verify the jwt token if run in a docker container.**
To develop the UI and integrate with the tdzm backend, the backend must be run natively on your machine.

# Building:

*If you did not run `npm install` before (e.g. when running in dev mode) you must run now!*

Option 1: Build and run without docker.
- Run `npm run build` from the ui folder. The build output will be html/css/js files located in the `ui/dist` folder.
- Then run `npm start` which will start the code in production mode and serve the static dist folder.

Option 2: Build and run with docker
- run `npm run build` from the ui folder.
- run `docker build -t <your tag> .` from the ui folder.

When building with docker, the output will be a new image based on node:lts-alpine. This image will be configured to serve the build output files and REST routes through port 80.

# Configuration:

Following options show how to modify the UI when running it through the docker image.

**Changing the Logo:**

If you want to modify the look by changing the logo you should mount your image into the docker container. Example:
`docker run --name zone-manager-ui -p 8080:80 -v <local logo path>:/app/dist/logo.png <image-name>`

**Configuring general Configuration:**
Confgurations are loaded from a .env file in the docker image. The default .env can be overwritten by mounting your desired config:
`docker run --name zone-manager-ui -p 8080:80 -v <local config path>:/app/.env <image-name>`

Alternatively you can specify the desired environment variables directly.


## Configuration reference:
Fields you can set in the .env file **or as environment variables**

Check the .env file directly for examples.

- ZONEMGR_URL (String) - Tells the UI where to fetch zone data from.
- OIDC_ISSUER_URL (String) - Tells the UI which OIDC Issuer to use. Will be used to autodiscover the required OIDC endpoints
- OIDC_SPECIAL_AUTH_HOST (String) - Use this if the oidc server is reachable under a different hostname from the browser, than from the ui host.
- OIDC_CLIENT_ID (String) the OIDC Client ID the frontend should use to initiate the Oauth2 Flow
- OIDC_CLIENT_SECRET (String) the OIDC Client Secret the frontend should use to initiate the Oauth2 Flow. **Should be configured at runtime through something like Kubernetes secrets or hashicorp vault!**
- OIDC_SCOPES (String) - Which OIDC scopes to request through the Oauth2 Flow
- UI_HOST (String) - The host address where the UI will be reachable.
- COOKIE_SECRET (String) - Random String of 32 base64 encoded to encrypt the cookie. **Should be configured at runtime through something like Kubernetes secrets or hashicorp vault!**
- NODE_TLS_REJECT_UNAUTHORIZED (String) - Tells the backend to not-check SSL Certs when connecting to the OIDC Server, or the TDZM. **USE ONLY FOR LOCAL DEVELOPMENT or non-prod environments!**
- APP_BASE_URL_PATH (String) - If you plan to run the UI under a sub-path, you need to supply the URI Path prefix here with a leading slash.
