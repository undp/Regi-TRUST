'use strict';
var express = require('express');
var router = express.Router();
var { getRoles } = require('../../auth/keycloak')
var getSubmissionFormat = require('../../data/submissionFormatting/submissionFormatting')

var trainApi = require('../../data/TRAIN/trainApiService');

/* GET list of all entries (table) page. */
router.get('/list', async (req, res, next) => {
    let error
    
    const trustList = await trainApi.getTrustList()
        .then(data => {
            data = data.filter(tsp => tsp.TrustServiceProvider.UID !== 12345)
            
            return data.map(tsp => getSubmissionFormat.apiToView(tsp))            
        })
        .catch(err => error = err)

    if(error)
        return next(error)

    res.render('./networkEntries/networkEntriesList', {
        currentNavigationName: 'Network Entries',
        title: 'Network Entries',
        json: JSON.stringify(trustList),
        roles: getRoles(req)
    })
});

/* Get details of one entry */
router.get('/details/:UID', async (req, res, next) => {
    let error
    
    const tspDetail = await trainApi.getTspDetail(req.params['UID'])
        .then(data => getSubmissionFormat.apiToView(data))
        .then(tsp => getSubmissionFormat.jsonToDetailPage(tsp))
        .catch(err => error = err)

    if(error)
        return next(error)

    res.render('./networkEntries/networkEntryDetails', {
        currentNavigationName: 'Network Entries',
        title: 'Network Entry Details',
        json: tspDetail,
        roles: getRoles(req)
    })
});

/* Get verfiy page */
router.get('/Verify', function (req, res, next) {
    return res.render('./networkEntries/verifyNetworkEntry', {
        currentNavigationName: 'Verify',
        title: 'Verify',
        roles: getRoles(req)
    });
});

/* Post from verify page */
/* 
 * Verification logic to be added
 */
router.post('/Verify', function (req, res, next) {
    return res.render('./networkEntries/verificationResult', {
        currentNavigationName: 'Verify',
        title: 'Verify',
        IssuerName: req.body.IssuerName,
        TrustSchemePointer: req.body.TrustSchemePointer,
        roles: getRoles(req)
    });
});

module.exports = router;