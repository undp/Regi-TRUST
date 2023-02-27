'use strict';
var express = require('express');
const { getRoles } = require('../Auth/keycloak');
var router = express.Router();

/* GET home page. */
router.get('/', function (req, res) {
    res.render('index', { title: 'Regi-TRUST', currentNavigationName: 'Home', roles: getRoles(req) });
});

module.exports = router;
