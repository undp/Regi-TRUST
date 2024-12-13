'use strict'

var express = require('express');
var router = express.Router();
var EnrollModel = require('../../data/MongoDB/mongoose').EnrollModel;
var { checkAuthorized, getRoles, getUserId, getAuthorization, getReviewerEmails } = require('../../Auth/keycloak');
var getSubmissionFormat = require('../../data/submissionFormatting/submissionFormatting');
const { notifyEnrollmentRequestReviewed } = require('../../notifications/emailService');
const trainApi = require('../../data/TRAIN/trainApiService');
const { Submitter } = require('../../data/MongoDB/TSPSchema');

router.get('/', checkAuthorized(['Onboarding_manager']), async (req, res) => {
    let status = req.query.status ? req.query.status : 'pending'
    let filter = { "ReviewInfo.ReviewStatus": status !== 'all' ? status : { "$ne": "in progress" } }

    let roles = getRoles(req)    
    if(!roles.includes('Onboarding_manager')) {
        filter = { 'Submitter': getUserId(req) }
        status = 'all'
    }

    let submissions = await EnrollModel.find(filter)

    res.render('./reviewSubmissions/reviewEnrollmentRequests', { 
        selectedTab: status, 
        submissions: JSON.stringify(submissions),
        title: 'Submissions: ' + (status.charAt(0).toUpperCase() + status.slice(1)),
        currentNavigationName: roles.includes('Onboarding_manager') ? 'Review Submissions' : 'My Submissions',
        roles: roles })
})

router.get('/submission/:id', async (req, res, next) => {
    let submission = await EnrollModel.findById(req.params.id)
    
    let roles = getRoles(req)

    submission = await getSubmissionFormat.mongoToReview(submission, "enroll")  

    res.render('./reviewSubmissions/reviewEnrollmentRequest', {
        submission: submission,
        title: 'Review Submission',
        currentNavigationName: roles.includes('Onboarding_manager') ? 'Review Submissions' : 'My Submissions',
        roles: getRoles(req) })
})

router.get('/submission/:id/accept', checkAuthorized(['Onboarding_manager']), async (req, res) => {     
    let submission = await EnrollModel.findById(req.params.id)
    let isReviewed = await submitReview(req.params.id, getUserId(req), "approved")

    if(!isReviewed)
        return res.render('./reviewSubmissions/reviewError', {
            submission_id: req.params.id,
            roles: getRoles(req),
            title: 'Review Error',
            currentNavigationName: 'Review Submissions'
        })
    else {
        submission = submission.toJSON();
        
        const submitterEmail = submission.TrustServiceProvider.SubmitterInfo.Address.ElectronicAddress
        const entityName = submission.TrustServiceProvider.SubmitterInfo.AgencyName
        const submissionId = submission.TrustServiceProvider.UID
        notifyEnrollmentRequestReviewed(submitterEmail, entityName, 'Approved', submissionId)
    } 

    res.json({
        success: true,
        redirect: '/review-enrollment-requests/' 
    })
})

router.post('/submission/:id/decline', checkAuthorized(['Onboarding_manager']), async (req, res) => {    
    let submission = await EnrollModel.findById(req.params.id)
    let isReviewed = await submitReview(req.params.id, getUserId(req), "rejected", req.body.Notes);    

    if(!isReviewed)
        return res.render('./reviewSubmissions/reviewError', {
            submission_id: req.params.id,
            roles: getRoles(req),
            title: 'Review Error',
            currentNavigationName: 'Review Submissions'
        })

    else {      
        const submitterEmail = submission.TrustServiceProvider.SubmitterInfo.Address.ElectronicAddress
        const entityName = submission.TrustServiceProvider.SubmitterInfo.AgencyName
        const submissionId = submission.TrustServiceProvider.UID
        notifyEnrollmentRequestReviewed(submitterEmail, entityName, 'Rejected', submissionId)
    } 

    res.json({
        success: true,
        redirect: '/review-enrollment-requests/' 
    })
})

const submitReview = async (id, user, status, notes="") => {
    let reviewSubmitted = true

    try {
        let submisionReview = await EnrollModel.findById(id)

        Object.assign(submisionReview.ReviewInfo, {
            ReviewStatus: status,
            StatusStartingTime: Date.now().toString(),
            Reviewer: user,
            Notes: notes
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
