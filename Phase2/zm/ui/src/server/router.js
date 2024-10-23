const express = require('express')
const openidClient = require('openid-client')
const session = require('express-session')
const MemoryStore = require('memorystore')(session)

require('dotenv').config()

const port = process.env.NODE_ENV === 'production' ? 80 : 8001

const ISSUER_URL = process.env.OIDC_ISSUER_URL
const CLIENT_ID = process.env.OIDC_CLIENT_ID
const CLIENT_SECRET = process.env.OIDC_CLIENT_SECRET
const UI_HOST = process.env.UI_HOST
const COOKIE_SECRET = process.env.COOKIE_SECRET

async function buildApp (app) {
  try {
    let issuer
    let client
    if (process.env.NODE_ENV !== 'test') {
      console.log(`Attempting Discovery of .well-known information for configured OIDC Issuer "${ISSUER_URL}"`)
      issuer = await openidClient.Issuer.discover(ISSUER_URL)
      console.log(`Successfully completed discovery for OIDC Issuer "${ISSUER_URL}`)
      client = new issuer.Client({
        client_id: CLIENT_ID,
        client_secret: CLIENT_SECRET,
        redirect_uris: [`${UI_HOST}${process.env.APP_BASE_URL_PATH || ''}/auth/callback`],
        response_types: ['code']
      })  
    }
    app.use(require('morgan')('tiny'))
    const sessionOpts = {
      secret: COOKIE_SECRET,
      store: new MemoryStore({
        checkPeriod: 86400000 // Prune entries every 24 hours.
      }),
      resave: false,
      saveUninitialized: true,
      cookie: {
        httpOnly: true,
        secure: false // False because we will run behind ssl terminated proxy.
      }
    }
    app.use(session(sessionOpts))

    app.use((_, res, next) => {
      res.locals = {
        oidc: {
          issuer: issuer,
          client: client
        }
      }
      next()
    })
    const basePath = process.env.APP_BASE_URL_PATH || '/'
    console.log('Starting Express with basePath: ' + basePath)
    app.use(basePath, defineRoutes())
    console.log('Express App built.')
    return app.listen(port, '0.0.0.0', () => {
      console.log(`Express app is listening on port ${port}`)
    })
  } catch (error) {
    console.log(`Error when starting App -> 
    Message: ${error.message}
    STACK: ${error.stack}`)
    process.exit(1)
  }
}
function defineRoutes() {
  const router = express.Router()
  const tokenMiddleware = require('./middleware/authTokenMiddleware')
  const zoneDataHandler = require('./handlers/zoneDataHandler')
  router.get('/api/zonedata', tokenMiddleware, zoneDataHandler.getZoneData)

  router.get('/auth/login', require('./handlers/authHandler').login)
  router.get('/auth/logout', tokenMiddleware, require('./handlers/authHandler').logout)
  router.get('/auth/callback', require('./handlers/authHandler').authCallback)
  return router
}
module.exports = buildApp