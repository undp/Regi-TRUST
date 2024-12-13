var router = require('express').Router()
var { checkAuthorized, authorize, redirect, deauthorize, getAccessToken } = require('../../Auth/keycloak')

router.get('/', authorize())

router.get('/callback/', (req, res, next) => {
    redirect()(req, res, next)
        
}, (req, res) => {
    req.session.accessToken = getAccessToken(req);
    res.redirect(req.session.returnTo)
})

router.get('/login', checkAuthorized(), (req, res) => {
    res.redirect('/')
})

router.get('/logout', deauthorize())

module.exports = router
