const { generators } = require('openid-client')
require('dotenv').config()

async function login(req, res) {
  try {
    const codeVerifier = generators.codeVerifier()
    const codeChallenge = generators.codeChallenge(codeVerifier)

    // Store code Verifier in cookie for later retrieval.
    req.session.codeVerifier = codeVerifier
    const specialAuthHost = process.env.OIDC_SPECIAL_AUTH_HOST
    const generatedAuthUrl = res.locals.oidc.client.authorizationUrl({
      scope: process.env.OIDC_SCOPES,
      resource: `${process.env.ZONEMGR_URL}`,
      code_challenge: codeChallenge,
      code_challenge_method: 'S256'
    })
    let authURL
    if (specialAuthHost === null || specialAuthHost === undefined) {
      authURL = generatedAuthUrl
    } else {
      authURL = generatedAuthUrl.replace(/\/\/.*:\d+\//, `//${specialAuthHost}/`)
    }
    console.log('Defined auth url as: ' + authURL)
    res.redirect(authURL)  
} catch (error) {
    res.status(500).send('Error: ' + error.message)
    console.log(`ERROR in /auth/login -> 
    MSG: ${error.message}
    STACK: ${error.stack}`)
  }
}

async function logout(req, res) {
  try {
    console.log('Not supported yet!')
  } catch (error) {
    res.status(500).send('Error: ' + error.message)
  }
}

async function authCallback(req, res) {
  try {
    const codeVerifier = req.session.codeVerifier
    const params = res.locals.oidc.client.callbackParams(req)
    const tokenSet = await res.locals.oidc.client.callback(res.locals.oidc.client.metadata.redirect_uris[0], params, {
      code_verifier: codeVerifier
    })
    req.session.accessToken = tokenSet.access_token
    req.session.idToken = tokenSet.id_token
    req.session.refreshToken = tokenSet.refresh_token
    req.session.expiryDate = tokenSet.expires_at
    req.session.save()
    // Login successful. Redirect to api/zonedata
    if (process.env.NODE_ENV !== 'production') console.log(`%%%%%%%%%%%%%%%%%%%%%%%%%%%
    ACCESS TOKEN: 
    ${tokenSet.access_token}
%%%%%%%%%%%%%%%%%%%%%%%%%%%`)
    res.redirect(`${process.env.APP_BASE_URL_PATH || '/'}`)
  } catch (error) {
    res.status(500).send('Error: ' + error.message)
    console.log(`ERROR in /auth/callback -> 
    MSG: ${error.message}
    STACK: ${error.stack}`)
  }
}


module.exports = {
  login,
  logout,
  authCallback
}