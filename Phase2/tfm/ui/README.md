# Requirements:

- node >= 18.0.0



# Running:

If running the first time you **must** run `npm install`

Then run `npm run dev` to start the development mode server and page.

# Building:

_If you did not run `npm install` before (e.g. when running in dev mode) you must run now!_

Option 1: Build without docker.

- Run `npm run build` from the ui folder. The build output will be html/css/js files located in the `ui/dist` folder.
- Then run `npm start` which will start the code in production mode and serve the static dist folder.

Option 2: Build with docker

- run `npm run build` from the ui folder.
- run `docker build -t <your tag> .` from the ui folder.

When building with docker, the output will be a new image based on node:lts-alpine. This image will be configured to serve the build output files and REST routes through port 80.

# Configuration:

Following options show how to modify the UI when running it through the docker image.

**Configuring general Configuration:**
Confgurations are loaded from a .env file in the docker image. The default .env can be overwritten by mounting your desired config:
`docker run --name tspa-ui -p 8080:80 -v <local config path>:/app/.env <image-name>`

## Configuration reference:

The following fields must be set in the .env file **or as environment variables**

Please keep the convention of using VITE\_ before the name of the variable.

- VITE_TSPA_BASE_URL

The following are the variables used for the authentication flow for end users. For example you can use keycloak. An example keycloak cofiguration realm [file](ui/docs/keaycloack-realm-example-for-tspa.json) can be found in the docs folder as reference.

- VITE_VITE_OIDC_ISSUER_URL -> OIDC compliant Identity Provider url
- VITE_REDIRECT_URL -> ui home
- VITE_CLIENT_ID -> e.g. "xfsctest"
- VITE_CLIENT_SECRET
- VITE_REALM -> e.g. "gxfs-dev-test"
- VITE_ROLE_ALLOWED -> e.g. "default-roles-gxfs-dev-test"
