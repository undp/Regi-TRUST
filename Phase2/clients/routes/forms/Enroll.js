'use strict';
var express = require('express');
var router = express.Router();
const axios = require('axios')
var formJSON = require('../../data/formFields/enroll');
var { getRoles } = require('../../Auth/keycloak');

var { EnrollModel } = require('../../data/MongoDB/mongoose');
var { notifyNewEnrollmentRequest } = require('../../notifications/emailService');
const { roleNames, enrollmentReviewStatuses } = require('../../Config/config.json');

const reCaptcha = require('../../Config/config.json').reCaptcha

/* GET enroll form page. */
router.get('/', function (req, res) {
    req.session.sessionData = null
    req.session.submitted = null

    res.redirect('/enroll/1')
});

router.get('/:step', function (req, res) {
    let step = parseInt(req.params['step']);

    let roles = getRoles(req);

    let sessionData = req.session.sessionData;
    if (!sessionData) {
        sessionData = {
            formData: {validation: formJSON.FormSections.map(() => 'true').slice(1)}
        };
        step = 1
    }

    if (step === formJSON.FormSections.length) {
        if (req.session.submitted) {
            req.session.sessionData = null;
            req.session.submitted = null;
        } else {
            return res.redirect('/enroll/' + (formJSON.FormSections.length - 1));
        }
    }

    res.render('./forms/enroll', {
        isOnEnrollmentPage: true,
        currentStep: step,
        validation: sessionData.formData.validation,
        formJSON: formJSON,
        currentNavigationName: getNavigationName(roles),
        title: 'Trust Service Provider Enrollment',
        roles: roles,
        reCaptcha
    });
});

/* Post TSP enrollmment request. */
router.post('/:step', async function (req, res, next) {    
    /***************************Verify reCaptcha*******************************/
    const response = req.body['g-recaptcha-response'];
    const secretKey = reCaptcha.secret;
    
    try {
        const verificationResponse = await axios.post(
            `https://www.google.com/recaptcha/api/siteverify`,
            null,
            {
                params: {
                    secret: secretKey,
                    response: response
                }
            }
        );
        const verificationResult = verificationResponse.data;
        
        if (!verificationResult.success) {
            return res.send('reCAPTCHA verification failed.'); // Send response and return to prevent further code execution
        }

    } catch (error) {        
        return res.send('An error occurred during verification: ' + error);
    }

    // Only continue if reCAPTCHA verification is successful
    let step = parseInt(req.params['step']);
    let nextStep = step + 1;


    let sessionData = req.session.sessionData;
    
    if (!sessionData) {
        sessionData = {
            formData: {}
        };

        req.session.sessionData = sessionData;
    }
    let formData = sessionData.formData;    
    sessionData.formData = mergeDeep(formData, req.body);

    if(req.body.SubmitType) {    
        req.session.submitted = await submitForm(sessionData.formData);

        if(!req.session.submitted){
            return res.render('./forms/submissionError',{
                roles: getRoles(req),
                title: 'Submission Error',
                currentNavigationName: getNavigationName(getRoles(req))
            })
        } else {
            const submitterEmail = formData.TrustServiceProvider.SubmitterInfo.Address.ElectronicAddress;
            const entityName = formData.TrustServiceProvider.SubmitterInfo.AgencyName;
            const submissionId = formData.TrustServiceProvider.UID;
            notifyNewEnrollmentRequest(submitterEmail, entityName, submissionId, req.session.serviceUserToken)
        }
    }else if(req.body.RedirectPage)
        nextStep = req.body.RedirectPage

    // Redirect only if no previous response was sent
    if (!res.headersSent) {
        res.redirect('/enroll/' + nextStep);
    }
});

const getNavigationName = (roles) => {
    return roles.includes(roleNames.SUBMITTER) ? 'Submit Enrollment Request' : 'Review Enrollment Requests'
}

async function submitForm(formData) {
    let isGoodSubmission = true

    if (isGoodSubmission) {
        const submittedTime = Date.now().toString()

        Object.assign(formData.TrustServiceProvider, {
            StatusStartDateTime: submittedTime
        })
        
        const reviewInfo = {
            SubmittedDateTime: submittedTime,
            ReviewStatus: enrollmentReviewStatuses.PENDING,
            StatusStartDateTime: submittedTime
        }

        try {
            let submission = new EnrollModel({ 
                TrustServiceProvider: formData.TrustServiceProvider,
                ReviewInfo: reviewInfo            
            })

            submission.TrustServiceProvider.UID = submission._id
            formData.TrustServiceProvider.UID = submission._id
            
            submission = await submission.save()
        } catch (err) {
            console.error(err)
            
            isGoodSubmission = false
        }
    }

    return isGoodSubmission
}

//Utility for merging nested objects with various value types
function mergeDeep(to, from) {
    const isObject = obj => obj && typeof obj === 'object';

    if(!(isObject(to) && isObject(from)))
            to = from

    else {
        Object.keys(from).forEach(key => {
            let tVal = to[key];
            let fVal = from[key];

            if (Array.isArray(tVal) && Array.isArray(fVal)) {
                //Merged array will have the same length as fVal
                if (fVal.length > tVal.length)
                    tVal = tVal.concat(fVal.slice(tVal.length))
                
                else
                    tVal = tVal.slice(0, fVal.length)
                
                //Assumes that array indices match - different values from index i of the array in "from"
                // replace those in index i of the array in "to"
                //Recursive call for possible array of objects
                to[key] = tVal.map((item, i) => mergeDeep(item, fVal[i]))
            }

            //Recursive call for nested objects
            else if (isObject(tVal) && isObject(fVal)) {
                to[key] = mergeDeep(tVal, fVal);
            }

            else {
                to[key] = fVal;
            }
        });
    }

    return to;
}

module.exports = router;
