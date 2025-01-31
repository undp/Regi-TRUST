'use strict';
var express = require('express');
var router = express.Router();
var { getRoles, checkAuthorized } = require('../../Auth/keycloak')

var trainApi = require('../../data/TRAIN/trainApiService');
const { jsonToDetailPage } = require('../../data/submissionFormatting/submissionFormatting');
const { roleNames } = require('../../Config/config.json')

/* GET list of all entries (table) page. */
router.get('/details', checkAuthorized([roleNames.SUBMITTER, roleNames.REVIEWER, roleNames.ADMIN]), async (req, res, next) => {
    let error
    let version = req.query.version;
    
    const trustList = await trainApi.getTrustList(version, req.session.accessToken)
        .then(res => {
            
            return jsonToDetailPage(res, "framework")
        })
        .catch(err => console.log(err))
        
    if(error)
        return next(error)

    res.render('./framework/framework', {
        currentNavigationName: 'Trust List Framework',
        title: 'Trust List Framqework',
        framework: trustList,
        version: version || trustList["Trust List Version"],
        roles: getRoles(req)
    })
});

module.exports = router;