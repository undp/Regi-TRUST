'use strict'

var express = require('express');
var router = express.Router();
var SubmissionModel = require('../../data/MongoDB/mongoose').SubmissionModel;
var { checkAuthorized, getRoles, getUserId } = require('../../Auth/keycloak');
var getSubmissionFormat = require('../../data/submissionFormatting/submissionFormatting');
const { notifySubmissionReviewed } = require('../../notifications/emailService');
const trainApi = require('../../data/TRAIN/trainApiService')
const { roleNames, networkSubmissionStatuses } = require('../../Config/config.json');

router.get('/', checkAuthorized([roleNames.SUBMITTER, roleNames.REVIEWER, roleNames.ADMIN]), async (req, res) => {
    let status = req.query.status ? req.query.status : networkSubmissionStatuses.PENDING
    let filter = status === 'all' ? {} : { "ReviewInfo.ReviewStatus": status }

    let currentRoles = getRoles(req)    
    if(currentRoles.includes(roleNames.SUBMITTER)) {
        filter = { 'Submitter.User_id': getUserId(req).User_id }
        status = 'All'
    }

    let submissions = await SubmissionModel.find(filter)

    res.render('./reviewSubmissions/reviewSubmissionsList', { 
        currentNavigationName: currentRoles.includes(roleNames.REVIEWER) || currentRoles.includes(roleNames.ADMIN) ? 'Review Submissions' : 'My Submissions',
        selectedTab: req.query.status || networkSubmissionStatuses.PENDING, 
        submissions: JSON.stringify(submissions),
        title: currentRoles.includes(roleNames.REVIEWER) || currentRoles.includes(roleNames.ADMIN) ? 'Review Submissions' : 'My Submissions',
        roles: currentRoles ,
        statuses: networkSubmissionStatuses})
})

router.get('/submission/:id', checkAuthorized([roleNames.SUBMITTER, roleNames.REVIEWER, roleNames.ADMIN]), async (req, res, next) => {
    let currentRoles = getRoles(req)
    const TSPVersions = await trainApi.getTspHistory(req.params.id, req.session.accessToken)        

    // By default, show the latest updated version
    let submission = await SubmissionModel.findOne({
        $or: [
            { "TrustServiceProvider.TSPID": req.params.id },
            { "_id": req.params.id }
        ]
    });
    let localRecord = submission;
    // else if version is queried, show that version
    if(req.query.version) {
        submission = await trainApi.getTspDetail(req.params.id, req.query.version, req.session.accessToken)
    }

    submission = await getSubmissionFormat.mongoToReview(submission)

    res.render('./reviewSubmissions/reviewSubmission', {
        submission: submission,
        title: 'Review Submission',
        currentNavigationName: currentRoles.includes(roleNames.REVIEWER) ? 'Review Submissions' : 'My Submissions',
        roles: currentRoles,
        TSPVersions: JSON.stringify(TSPVersions),
        currentVersion: req.query.version ? req.query.version : submission.ReviewInfo.ReviewStatus == networkSubmissionStatuses.APPROVED ? Math.max(...TSPVersions.versions.map(v => parseInt(v.TSPVersion, 10))) : null,
        statuses: networkSubmissionStatuses,
        hasPendingVersion: localRecord.ReviewInfo.ReviewStatus == networkSubmissionStatuses.PENDING
    })
})

router.get('/submission/:id/accept', checkAuthorized([roleNames.REVIEWER, roleNames.ADMIN]), async (req, res) => {    
    let accepted = true
    let submission = await SubmissionModel.findById(req.params.id)

    submission = submission.toObject();
    
    submission.TrustServiceProvider.LastUpdate = new Date(parseInt(submission.ReviewInfo.SubmittedDateTime, 10));
    submission.TrustServiceProvider.TSPInformation.TSPType = submission.TrustServiceProvider.TSPInformation.TSPRole;
    
    const isPublished = await trainApi.getTspDetail(submission.TrustServiceProvider.TSPID, null, req.session.accessToken);

    accepted = await submitReview(req.params.id, getUserId(req), networkSubmissionStatuses.APPROVED)
    accepted = accepted ? await trainApi.postRegistryEntry(submission, req.session.accessToken, isPublished) : accepted

    if(!accepted)
        return res.render('./reviewSubmissions/reviewError', {
            submission_id: req.params.id,
            roles: getRoles(req),
            title: 'Review Error',
            currentNavigationName: 'Review Submissions'
        })

    else {
        const submitterId = submission.Submitter.User_id
        const entityName = submission.TrustServiceProvider.TSPInformation.TSPName.Name
        const submissionId = submission.TrustServiceProvider.TSPID
        notifySubmissionReviewed(submitterId, entityName, networkSubmissionStatuses.APPROVED, submissionId)
    } 

    res.redirect('/review-submissions/')
})

router.get('/submission/:id/decline', checkAuthorized([roleNames.REVIEWER, roleNames.ADMIN]), async (req, res) => {    
    let declined = await submitReview(req.params.id, getUserId(req), networkSubmissionStatuses.REJECTED)
    let submission = await SubmissionModel.findById(req.params.id)

    if(!declined)
        return res.render('./reviewSubmissions/reviewError', {
            submission_id: req.params.id,
            roles: getRoles(req),
            title: 'Review Error',
            currentNavigationName: 'Review Submissions'
        })

        else {
            const submitterId = submission.Submitter.User_id
            const entityName = submission.TrustServiceProvider.TSPInformation.TSPName.Name
            const submissionId = submission.TrustServiceProvider.TSPID
            notifySubmissionReviewed(submitterId, entityName, networkSubmissionStatuses.REJECTED, submissionId)
        } 
    
    res.redirect('/review-submissions/')
})

router.get('/submission/:id/edit', checkAuthorized([roleNames.SUBMITTER, roleNames.REVIEWER, roleNames.ADMIN]), async (req, res, next) => {
    let submission = await SubmissionModel.findById(req.params.id)

    if(!(getRoles(req).includes(roleNames.REVIEWER) || getRoles(req).includes(roleNames.ADMIN)) && submission.Submitter.User_id !== getUserId(req).User_id) {
        let err = new Error('Forbidden')
        err.status = 403
        return next(err)
    }

    submission = getSubmissionFormat.mongoToForm(submission)
    
    req.session.sessionData = {
        formData: submission,
        editing: true
    }
    
    res.redirect('/forms/gccn-network-entry-submission/1')
})

const submitReview = async (id, user, status) => {
    let reviewSubmitted = true

    try {
        let submisionReview = await SubmissionModel.findById(id)

        Object.assign(submisionReview.ReviewInfo, {
            ReviewStatus: status,
            StatusStartingTime: Date.now().toString(),
            Reviewer: user
        })

        submisionReview.save()
    }

    catch (err) {
        console.error(err)

        reviewSubmitted = false
    }

    return reviewSubmitted
}

module.exports = router;
