var router = require('express').Router()
var { checkAuthorized, authorize, redirect, deauthorize, getAccessToken, getServiceUserToken } = require('../../Auth/keycloak')

router.get('/', authorize())

router.get('/callback/', (req, res, next) => {
    redirect()(req, res, next)
        
}, async (req, res) => {
    req.session.accessToken = await getAccessToken(req);
    req.session.serviceUserToken = await getServiceUserToken(req.session.accessToken);
    res.redirect(req.session.returnTo)
})

router.get('/login', checkAuthorized(), (req, res) => {
    res.redirect('/')
})

router.get('/logout', deauthorize())

module.exports = router
