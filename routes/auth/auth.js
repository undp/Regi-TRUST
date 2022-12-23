var router = require('express').Router()
var { checkAuthorized, authorize, redirect, deauthorize } = require('../../auth/keycloak')

router.get('/', authorize())

router.get('/callback/', (req, res, next) => {
    redirect()(req, res, next)
        
}, (req, res) => {
    res.redirect(req.session.returnTo)
})

router.get('/login', checkAuthorized(), (req, res) => {
    res.redirect('/')
})

router.get('/logout', deauthorize())

module.exports = router
