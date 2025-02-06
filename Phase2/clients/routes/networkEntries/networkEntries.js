'use strict';
var express = require('express');
var router = express.Router();
var { getRoles, checkAuthorized } = require('../../Auth/keycloak')
var getSubmissionFormat = require('../../data/submissionFormatting/submissionFormatting')

var trainApi = require('../../data/TRAIN/trainApiService');
var roleNames = require('../../Config/config.json').roleNames;

/* GET list of all entries (table) page. */
router.get('/list', checkAuthorized([roleNames.SUBMITTER, roleNames.REVIEWER, roleNames.ADMIN]), async (req, res, next) => {
    let error
    let version = req.query.version;
    
    const trustList = await trainApi.getTrustList(version ? version : null, req.session.accessToken)
        .then(res => {
            if(!version)
                version = res.FrameworkInformation?.TSLVersion;
            
            return res.TSPSimplifiedList?.TSPSimplified
        })
        .catch(err => console.log(err))

    const trustListVersions = await trainApi.getTrustListVersions(req.session.accessToken)
        .then(res => {
            return res.trustListVersions
        })
        .catch(err => error = err)

    if(error)
        return next(error)

    res.render('./networkEntries/networkEntriesList', {
        currentNavigationName: 'Network Entries',
        title: 'Network Entries',
        TSPs: JSON.stringify(trustList || []),
        TSPVersions: JSON.stringify(trustListVersions),
        selectedVersion: version,
        roles: getRoles(req)
    })
});

/* Get details of one entry */
router.get('/details/:TSPID', checkAuthorized([roleNames.SUBMITTER, roleNames.REVIEWER, roleNames.ADMIN]), async (req, res, next) => {
    let error
    let selectedRecord = {id: req.params.TSPID, version: req.query.version}
    
    const tspDetail = await trainApi.getTspDetail(selectedRecord.id, selectedRecord.version ? selectedRecord.version : null, req.session.accessToken)
        .then(res => {
            if(!selectedRecord.version)
                selectedRecord.version = res.TSPVersion;

            return res
        })
        .then(tsp => getSubmissionFormat.jsonToDetailPage(tsp))
        .catch(err => error = err)

    const tspHistory = await trainApi.getTspHistory(selectedRecord.id, req.session.accessToken)
        .then(res => {
            return res.versions
        })
        .catch(err => error = err)

    if(error)
        return next(error)

    res.render('./networkEntries/networkEntryDetails', {
        currentNavigationName: 'Network Entries',
        title: 'Network Entry Details',
        json: tspDetail,
        tspHistory: JSON.stringify(tspHistory),
        selectedRecord: selectedRecord, 
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