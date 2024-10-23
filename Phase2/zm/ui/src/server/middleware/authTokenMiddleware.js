const jwt = require('jsonwebtoken')

/**
 * 
 * @param {Express.Request} req the request
 * @param {Express.Response} res the response object
 * @param {Function} next function to advance in the chain.
 */
async function checkIfAccessTokenExists (req, res, next) {
  if (process.env.NODE_ENV === 'test') {
    req.session.accessToken = ''
    next()
    return
  }
  try {
    // If tokenset exists:
    const expiryDate = req.session.expiryDate
    if (expiryDate === null || expiryDate === undefined) {
      throw new Error(`No Tokens for Session: ${req.sessionID}`)
    } else if (expiryDate < Date.now()) {
      if (req.session.refreshToken === null || req.session.refreshToken === undefined) {
        req.session.destroy()
        throw new Error(`No Refresh token, but expiry date is set for Session ${req.sessionID}`)
      }
      const tokenSet = await res.locals.oidc.client.refresh(req.session.refreshToken)
      req.session.accessToken = tokenSet.access_token
      req.session.idToken = tokenSet.id_token
      req.session.refreshToken = tokenSet.refresh_token
      next()
    } else {
      next()
    }
  } catch (error) {
    res.redirect(`${process.env.APP_BASE_URL_PATH || ''}/auth/login`)
  }
}

module.exports = checkIfAccessTokenExists