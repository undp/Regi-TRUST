'use strict'

var express = require('express');
var router = express.Router();
var SubmissionModel = require('../../data/MongoDB/mongoose').SubmissionModel;
var { checkAuthorized, getRoles, getUserId, getAuthorization } = require('../../Auth/keycloak');
var getSubmissionFormat = require('../../data/submissionFormatting/submissionFormatting');
const { notifySubmissionReviewed } = require('../../notifications/emailService');
const trainApi = require('../../data/TRAIN/trainApiService')

router.get('/', checkAuthorized(['Registry_reviewer', 'Registry_submitter']), async (req, res) => {
    let status = req.query.status ? req.query.status : 'pending'
    let filter = { "ReviewInfo.ReviewStatus": status !== 'all' ? status : { "$ne": "in progress" } }

    let roles = getRoles(req)    
    if(!roles.includes('Registry_reviewer')) {
        filter = { 'Submitter': getUserId(req) }
        status = 'all'
    }

    let submissions = await SubmissionModel.find(filter)

    res.render('./reviewSubmissions/reviewSubmissionsList', { 
        selectedTab: status, 
        submissions: JSON.stringify(submissions),
        title: 'Submissions: ' + (status.charAt(0).toUpperCase() + status.slice(1)),
        currentNavigationName: roles.includes('Registry_reviewer') ? 'Review Submissions' : 'My Submissions',
        roles: roles })
})

router.get('/submission/:id', checkAuthorized(['Registry_reviewer', 'Registry_submitter']), async (req, res, next) => {
    let submission = await SubmissionModel.findById(req.params.id)
    let roles = getRoles(req)

    if(!roles.includes('Registry_reviewer') && submission.Submitter.User_id !== getUserId(req).User_id) {
        let err = new Error('Forbidden')
        err.status = 403
        return next(err)
    }

    submission = await getSubmissionFormat.mongoToReview(submission)

    res.render('./reviewSubmissions/reviewSubmission', {
        submission: submission,
        title: 'Review Submission',
        currentNavigationName: roles.includes('Registry_reviewer') ? 'Review Submissions' : 'My Submissions',
        roles: getRoles(req) })
})

router.get('/submission/:id/accept', checkAuthorized(['Registry_reviewer']), async (req, res) => {    
    let accepted = true
    let submission = await SubmissionModel.findById(req.params.id)
    
    accepted = await submitReview(req.params.id, getUserId(req), "accepted")
    accepted = accepted ? await trainApi.putRegistryEntry(submission, getAuthorization({req})?.tokenSet?.access_token) : accepted

    if(!accepted)
        return res.render('./reviewSubmissions/reviewError', {
            submission_id: req.params.id,
            roles: getRoles(req),
            title: 'Review Error',
            currentNavigationName: 'Review Submissions'
        })

    else {
        const submitterEmail = submission.TrustServiceProvider.SubmitterInfo.Address.ElectronicAddress
        const entityName = submission.TrustServiceProvider.TSPInformation.TSPName
        const submissionId = submission.TrustServiceProvider.UID
        notifySubmissionReviewed(submitterEmail, entityName, 'Accepted', submissionId)
    } 

    res.redirect('/review-submissions/')
})

router.get('/submission/:id/decline', checkAuthorized(['Registry_reviewer']), async (req, res) => {    
    let declined = await submitReview(req.params.id, getUserId(req), "rejected")
    let submission = await SubmissionModel.findById(req.params.id)

    if(!declined)
        return res.render('./reviewSubmissions/reviewError', {
            submission_id: req.params.id,
            roles: getRoles(req),
            title: 'Review Error',
            currentNavigationName: 'Review Submissions'
        })

        else {
            const submitterEmail = submission.TrustServiceProvider.SubmitterInfo.Address.ElectronicAddress
            const entityName = submission.TrustServiceProvider.TSPInformation.TSPName
            const submissionId = submission.TrustServiceProvider.UID
            notifySubmissionReviewed(submitterEmail, entityName, 'Declined', submissionId)
        } 
    
    res.redirect('/review-submissions/')
})

router.get('/submission/:id/edit', checkAuthorized(['Registry_reviewer', 'Registry_submitter']), async (req, res, next) => {
    let submission = await SubmissionModel.findById(req.params.id)

    if(!getRoles(req).includes('Registry_reviewer') && submission.Submitter.User_id !== getUserId(req).User_id) {
        let err = new Error('Forbidden')
        err.status = 403
        return next(err)
    }

    submission = getSubmissionFormat.mongoToForm(submission)
    
    req.session.sessionData = {
        formData: submission,
        editing: true
    }
    
    res.redirect('/form/gccn-network-entry-submission/1')
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
