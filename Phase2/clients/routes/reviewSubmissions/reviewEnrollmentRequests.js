'use strict'

var express = require('express');
var router = express.Router();
var EnrollModel = require('../../data/MongoDB/mongoose').EnrollModel;
var { checkAuthorized, getRoles, getUserId } = require('../../Auth/keycloak');
var getSubmissionFormat = require('../../data/submissionFormatting/submissionFormatting');
const { notifyEnrollmentRequestApproved } = require('../../notifications/emailService');
var { roleNames, enrollmentReviewStatuses } = require('../../Config/config.json');

router.get('/', checkAuthorized([roleNames.ONBOARDING_MANAGER, roleNames.ADMIN]), async (req, res) => {
    let status = req.query.status ? req.query.status : enrollmentReviewStatuses.PENDING
    let filter = { "ReviewInfo.ReviewStatus": status !== 'all' ? status : { "$ne": "in progress" } }

    let currentRoles = getRoles(req)    
    let submissions = await EnrollModel.find(filter)

    res.render('./reviewSubmissions/reviewEnrollmentRequests', { 
        selectedTab: status, 
        submissions: JSON.stringify(submissions),
        title: 'Enrollments: ' + (status.charAt(0).toUpperCase() + status.slice(1)),
        currentNavigationName: 'Review Enrollment Requests',
        roles: currentRoles ,
        statuses: enrollmentReviewStatuses})
})

router.get('/submission/:id', checkAuthorized([roleNames.ONBOARDING_MANAGER, roleNames.ADMIN]), async (req, res, next) => {
    let submission = await EnrollModel.findById(req.params.id)
    
    submission = await getSubmissionFormat.mongoToReview(submission, "enroll")  

    res.render('./reviewSubmissions/reviewEnrollmentRequest', {
        submission: submission,
        title: 'Review Submission',
        currentNavigationName: 'Review Enrollment Requests',
        roles: getRoles(req) ,
        statuses: enrollmentReviewStatuses})
})

router.get('/submission/:id/accept', checkAuthorized([roleNames.ONBOARDING_MANAGER, roleNames.ADMIN]), async (req, res) => {     
    let submission = await EnrollModel.findById(req.params.id)
    let isReviewed = await submitReview(req.params.id, getUserId(req), enrollmentReviewStatuses.APPROVED);

    if(!isReviewed)
        return res.render('./reviewSubmissions/reviewError', {
            submission_id: req.params.id,
            roles: getRoles(req),
            title: 'Review Error',
            currentNavigationName: 'Review Enrollment Requests',
        })
    else {
        submission = submission.toJSON();
        
        const submitterEmail = submission.TrustServiceProvider.SubmitterInfo.Address.ElectronicAddress
        const entityName = submission.TrustServiceProvider.SubmitterInfo.AgencyName
        notifyEnrollmentRequestApproved(submitterEmail, entityName)
    } 

    res.json({
        success: true,
        redirect: '/review-enrollment-requests/' 
    })
})

router.post('/submission/:id/decline', checkAuthorized([roleNames.ONBOARDING_MANAGER, roleNames.ADMIN]), async (req, res) => {    
    let isReviewed = await submitReview(req.params.id, getUserId(req), enrollmentReviewStatuses.REJECTED, req.body.Notes);    

    if(!isReviewed)
        return res.render('./reviewSubmissions/reviewError', {
            submission_id: req.params.id,
            roles: getRoles(req),
            title: 'Review Error',
            currentNavigationName: 'Review Enrollment Requests',
        })

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
