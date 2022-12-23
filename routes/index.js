'use strict';
var express = require('express');
const { getRoles } = require('../auth/keycloak');
var router = express.Router();

/* GET home page. */
router.get('/', function (req, res) {
    res.render('index', { title: 'GCCN Trust Registry Network', currentNavigationName: 'Home', roles: getRoles(req) });
});

module.exports = router;
