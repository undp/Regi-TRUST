'use strict';
var express = require('express');
var router = express.Router();
var formJSON = require('../../data/formFields');
var { checkAuthorized, getRoles, getUserId } = require('../../auth/keycloak');
var { SubmissionModel } = require('../../data/MongoDB/mongoose');
var submissionFormat = require('../../data/submissionFormatting/submissionFormatting');
var { notifyNewSubmission, notifySubmissionUpdated } = require('../../notifications/emailService')

router.get('/', checkAuthorized(['Registry_submitter', 'Registry_reviewer']), function(req, res) {
    req.session.sessionData = null
    req.session.submitted = null

    res.redirect('/form/gccn-network-entry-submission/1')
})

/* GET form page. */
router.get('/:step', checkAuthorized(['Registry_submitter', 'Registry_reviewer']), function (req, res, next) {
    let step = parseInt(req.params['step']);

    let sessionData = req.session.sessionData;
    if (!sessionData) {
        sessionData = {
            formData: {validation: formJSON.FormSections.map(() => 'true').slice(1)},
            visited: 1,
            editing: false
        };

        step = 1
    }

    else if (sessionData.editing && !sessionData.visited) {
        sessionData.formData.validation = formJSON.FormSections.map(() => 'true').slice(1)
        sessionData.visited = formJSON.FormSections.length
    }

    let roles = getRoles(req)
    if(!roles.includes('Registry_submitter') && !sessionData.editing) {
        req.session.sessionData = null
        let err = new Error('Forbidden')
        err.status = 403
        return next(err)
    }

    let visited = sessionData.visited
    if(step > visited && sessionData.formData.validation[visited - 1] === 'true') {
        if (step >= visited + 2 )
            step = visited

        sessionData.visited = step
    }

    req.session.sessionData = sessionData;

    if (step === formJSON.FormSections.length) {
        if (req.session.submitted) {
            req.session.sessionData = null;
            req.session.submitted = null;
        } 
        
        else {
            return res.redirect('/form/gccn-network-entry-submission/' + (formJSON.FormSections.length - 1));
        }
    }

    let [ submitTemplate, editTemplate ] = [ './form/networkEntrySubmission', './form/editNetworkEntrySubmission' ]

    res.render(sessionData.editing ? editTemplate : submitTemplate , {
        currentStep: step,
        visited: sessionData.visited,
        validation: sessionData.formData.validation,
        formJSON: formJSON,
        formData: sessionData.formData,
        currentNavigationName: getNavigationName(roles, sessionData.editing),
        title: sessionData.editing ? 'Edit Submission' : 'Submit Network Entry',
        roles: roles
    });
});

/* Post form page. */
router.post('/:step', checkAuthorized(['Registry_submitter','Registry_reviewer']), async function (req, res, next) {
    let step = parseInt(req.params['step']);
    
    let sessionData = req.session.sessionData;
    if (!sessionData) {
        sessionData = {
            formData: {}
        };
        
        req.session.sessionData = sessionData;
    }

    let roles = getRoles(req)
    if(!roles.includes('Registry_submitter') && !sessionData.editing) {
        req.session.sessionData = null
        let err = new Error('Forbidden')
        err.status = 403
        return next(err)
    }

    let formData = sessionData.formData;    
    sessionData.formData = mergeDeep(formData, req.body);
    
    let nextStep
    if(req.body.SubmitType) {    
        switch (req.body.SubmitType) {
            case 'Previous': {
                nextStep = step - 1 
                break
            }

            case 'Next': {
                nextStep = step + 1
                break
            }
            
            case 'Submit': {
                nextStep = step + 1

                if(nextStep === formJSON.FormSections.length) {
                    req.session.submitted = await submitForm(sessionData.formData, getUserId(req));

                    if(!req.session.submitted)
                        return res.render('./form/submissionError',{
                            roles: getRoles(req),
                            title: 'Submission Error',
                            currentNavigationName: getNavigationName(roles, sessionData.editing)
                        })

                    else {
                        const entityName = formData.TrustServiceProvider.TSPInformation.TSPName
                        const submissionId = formData.TrustServiceProvider.UID
                        notifyNewSubmission(entityName, submissionId)
                    }
                }
                
                break
            }

            case 'Save': {
                req.session.submitted = await updateSubmission(sessionData.formData)

                if(!req.session.submitted)
                    return res.render('./form/submissionError', {
                        roles: getRoles(req),
                        title: 'Submission Error',
                        currentNavigationName: getNavigationName(roles, sessionData.editing)
                    })

                else if(roles.includes('Registry_reviewer')) {
                    const submitterEmail = formData.TrustServiceProvider.SubmitterInfo.Address.ElectronicAddress
                    const entityName = formData.TrustServiceProvider.TSPInformation.TSPName
                    const submissionId = formData.TrustServiceProvider.UID                    
                    notifySubmissionUpdated(submitterEmail, entityName, submissionId)
                }
            }

            case 'Cancel': {
                req.session.sessionData = null

                return res.redirect('/review-submissions/')
            }

            default: break
        }
    }

    else if(req.body.RedirectPage)
        nextStep = req.body.RedirectPage

    res.redirect('/form/gccn-network-entry-submission/' + nextStep);
});

const getNavigationName = (roles, editing) => {
    let navName

    if (roles.includes('Registry_submitter')) {
        if (editing) {
            if (roles.includes('Registry_reviewer'))
                navName = 'Review Submissions'

            else navName = 'My Submissions'
        }

        else navName = 'Submit Network Entry'
    }

    else navName = 'Review Submissions'

    return navName
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

async function updateSubmission(formData) {
    formData = submissionFormat.formToMongo(formData)

    let updated = true
    let submission = await SubmissionModel.findById(formData._id)
    
    try {
        submission.overwrite(formData)
        await submission.save()
    }

    catch (err) {
        console.error(err)

        updated = false
    }

    return updated
}

async function submitForm(formData, submitter) {
    let isGoodSubmission = true
    
    try {
        formData = submissionFormat.formToMongo(formData)
    }

    catch (err) {
        console.error(err)

        isGoodSubmission = false
    }


    if (isGoodSubmission) {
        const submittedTime = Date.now().toString()

        Object.assign(formData.TrustServiceProvider, {
            TSPCurrentStatus: "new",
            StatusStartDateTime: submittedTime
        })
        
        const reviewInfo = {
            SubmittedDateTime: submittedTime,
            ReviewStatus: "pending",
            StatusStartDateTime: submittedTime
        }

        try {
            let submission = new SubmissionModel({ 
                TrustServiceProvider: formData.TrustServiceProvider,
                Submitter: submitter, 
                ReviewInfo: reviewInfo            
            })

            submission.TrustServiceProvider.UID = submission._id
            formData.TrustServiceProvider.UID = submission._id
            submission = await submission.save()
        }

        catch (err) {
            console.error(err)
            
            isGoodSubmission = false
        }
    }

    return isGoodSubmission
}

module.exports = router;
