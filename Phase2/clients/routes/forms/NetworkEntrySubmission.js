'use strict';
var express = require('express');
var router = express.Router();
var formJSON = require('../../data/formFields/networkEntrySubmission');
var { checkAuthorized, getRoles, getUserId } = require('../../Auth/keycloak');
var { SubmissionModel } = require('../../data/MongoDB/mongoose');
var submissionFormat = require('../../data/submissionFormatting/submissionFormatting');
var { notifyNewSubmission, notifySubmissionUpdated } = require('../../notifications/emailService')
var trainApi = require('../../data/TRAIN/trainApiService');
const { roleNames, networkSubmissionStatuses } = require('../../Config/config.json');

router.get('/', checkAuthorized([roleNames.SUBMITTER, roleNames.REVIEWER, roleNames.ADMIN]), function(req, res) {
    req.session.sessionData = null
    req.session.submitted = null

    res.redirect('/forms/gccn-network-entry-submission/1')
})

/* GET form page. */
router.get('/:step', checkAuthorized([roleNames.SUBMITTER, roleNames.REVIEWER, roleNames.ADMIN]), async function (req, res, next) {
    let step = parseInt(req.params['step']);
    let tsp = req.query.tsp;
    let version = req.query.version;

    let tspDetails = null;
    let sessionData = req.session.sessionData;

    //if edit form, get tsp details to populate the forms
    if(version || tsp) {
        sessionData = {
            formData: {
                validation: formJSON.FormSections.map(() => 'true').slice(1),
            },
            visited: 1,
            editing: true
        };

        if(version) {
            tspDetails = await trainApi.getTspDetail(tsp, version ? version : null, req.session.accessToken)
            sessionData.formData.TrustServiceProvider = tspDetails
        }else{
            tspDetails = await SubmissionModel.findById(tsp)
            sessionData.formData.TrustServiceProvider = tspDetails.TrustServiceProvider
            sessionData.formData.ReviewInfo = tspDetails.ReviewInfo
            sessionData.formData.Submitter = tspDetails.Submitter
        }
    }
        
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

    let currentRoles = getRoles(req)
    if(!(currentRoles.includes(roleNames.SUBMITTER) || currentRoles.includes(roleNames.ADMIN)) && !sessionData.editing) {
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
            return res.redirect('/forms/gccn-network-entry-submission/' + (formJSON.FormSections.length - 1));
        }
    }

    let [ submitTemplate, editTemplate ] = [ './forms/networkEntrySubmission', './forms/editNetworkEntrySubmission' ]

    res.render(sessionData.editing ? editTemplate : submitTemplate , {
        showAddServiceForm: false,
        currentStep: step,
        visited: sessionData.visited,
        validation: sessionData.formData.validation,
        formJSON: formJSON,
        formData: sessionData.formData,
        isEditing: false,
        serviceIndex: 0,
        currentNavigationName: getNavigationName(currentRoles, sessionData.editing),
        title: sessionData.editing ? 'Edit Submission' : 'Submit Network Entry',
        roles: currentRoles,
        tspDetails: tspDetails
    });
});

/* GET form page. */
router.get('/:step/add-service', checkAuthorized([roleNames.SUBMITTER, roleNames.REVIEWER, roleNames.ADMIN]), async function (req, res, next) {
    let step = parseInt(req.params['step']);
    let sessionData = req.session.sessionData;
    if (!sessionData) {
        sessionData = {
            formData: {validation: formJSON.FormSections.map(() => 'true').slice(1)},
            visited: 1,
            editing: false
        };
    }
    
    res.render('./forms/networkEntrySubmission' , {
        isEditing: false,
        serviceIndex: 0,
        showAddServiceForm: true,
        currentStep: step,
        formJSON: formJSON,
        roles: getRoles(req),
        validation: sessionData.formData.validation,
    });
});

router.get('/:step/edit-service', checkAuthorized([roleNames.SUBMITTER, roleNames.REVIEWER, roleNames.ADMIN]), (req, res, next) => {
    let step = parseInt(req.params['step']);
    let sessionData = req.session.sessionData;

    if (!sessionData) {
        sessionData = {
            formData: {validation: formJSON.FormSections.map(() => 'true').slice(1)},
            visited: 1,
            editing: true
        };
    }

    res.render('./forms/networkEntrySubmission' , {
        showAddServiceForm: true,
        isEditing: true,
        serviceIndex: req.query.index,
        formData: sessionData.formData,
        currentStep: step,
        formJSON: formJSON,
        roles: getRoles(req),
        validation: sessionData.formData.validation,
    });
});
router.get('/:step/remove-service', (req, res) => {
    const rowIndex = req.query.index;   

    const services = req.session.sessionData.formData.TrustServiceProvider.TSPServices.TSPService;
      
    if (rowIndex >= 0 && rowIndex < services.length) {
        // Remove the service from the array
        services.splice(rowIndex, 1);
    }

    req.session.sessionData.formData.TrustServiceProvider.TSPServices.TSPService = services;

    res.redirect('/forms/gccn-network-entry-submission/' + (formJSON.FormSections.length - 1));
});

/* Post form page. */
router.post('/:step', checkAuthorized([roleNames.SUBMITTER, roleNames.REVIEWER, roleNames.ADMIN]), async function (req, res, next) {
    let step = parseInt(req.params['step']);
    
    let sessionData = req.session.sessionData;
    if (!sessionData) {
        sessionData = {
            formData: {}
        };
        
        req.session.sessionData = sessionData;
    }

    let currentRoles = getRoles(req)
    if(!(currentRoles.includes(roleNames.SUBMITTER) || currentRoles.includes(roleNames.ADMIN)) && !sessionData.editing) {
        req.session.sessionData = null
        let err = new Error('Forbidden')
        err.status = 403
        return next(err)
    }

    let formData = sessionData.formData;    
    sessionData.formData = mergeDeep(formData, req.body);
    
    let nextStep = step
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

            case 'Edit': {
                const rowIndex = req.query.serviceIndex;   
                const services = req.session.sessionData.formData.TrustServiceProvider.TSPServices.TSPService;
                
                if (rowIndex >= 0 && rowIndex < services.length) {
                    // Remove the service from the array
                    services.splice(rowIndex, 1);
                }
                req.session.sessionData.formData.TrustServiceProvider.TSPServices.TSPService = services;
            }
            case 'Submit': {
                nextStep = req.query?.redirectPage || step + 1

                if(nextStep === formJSON.FormSections.length) {
                    req.session.submitted = await submitForm(sessionData.formData, getUserId(req));

                    if(!req.session.submitted)
                        return res.render('./forms/submissionError',{
                            roles: currentRoles,
                            title: 'Submission Error',
                            currentNavigationName: getNavigationName(currentRoles, sessionData.editing)
                        })

                    else {
                        const entityName = formData.TrustServiceProvider.TSPInformation.TSPName.Name
                        const submissionId = formData.TrustServiceProvider.TSPID
                        notifyNewSubmission(entityName, submissionId, req.session.serviceUserToken)
                    }
                }
                
                break
            }

            case 'Save': {
                req.session.submitted = await updateSubmission(sessionData.formData)

                if(!req.session.submitted)
                    return res.render('./forms/submissionError', {
                        roles: currentRoles,
                        title: 'Submission Error',
                        currentNavigationName: getNavigationName(currentRoles, sessionData.editing)
                    })

                else{
                    let submitterId = formData.Submitter?.User_id
                    if(!submitterId){
                        let localRecord = await SubmissionModel.findOne({ 'TrustServiceProvider.TSPID': formData.TrustServiceProvider.TSPID })
                        submitterId = localRecord.Submitter.User_id
                    }
                    const entityName = formData.TrustServiceProvider.TSPInformation.TSPName.Name
                    const submissionId = formData.TrustServiceProvider.TSPID                  
                    if (currentRoles.includes(roleNames.REVIEWER) || currentRoles.includes(roleNames.ADMIN)){
                        notifySubmissionUpdated(submitterId, entityName, submissionId, false)
                    }else if(currentRoles.includes(roleNames.SUBMITTER)){
                        notifySubmissionUpdated(submitterId, entityName, submissionId, true)
                    }
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

    res.redirect('/forms/gccn-network-entry-submission/' + nextStep);
});

const getNavigationName = (currentRoles, editing) => {
    let navName

    if (currentRoles.includes(roleNames.SUBMITTER) || currentRoles.includes(roleNames.REVIEWER) || currentRoles.includes(roleNames.ADMIN)) {
        if (editing) {
            if (currentRoles.includes(roleNames.REVIEWER))
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

    if(!(isObject(to) && isObject(from))) {
        return from; // Replace primitive or non-object values
    }

    Object.keys(from).forEach(key => {
        let tVal = to[key];
        let fVal = from[key];

        // Special handling for nested TSPServices.TSPService array
        if (
            key === 'TSPServices' &&
            isObject(tVal) &&
            isObject(fVal) &&
            Array.isArray(tVal.TSPService) &&
            Array.isArray(fVal.TSPService)
        ) {
            // Append elements from fVal.TSPService to tVal.TSPService
            to[key].TSPService = tVal.TSPService.concat(fVal.TSPService);
        }
        // Recursively merge nested objects
        else if (isObject(tVal) && isObject(fVal)) {
            to[key] = mergeDeep(tVal, fVal);
        }
        // Replace primitive values or non-object types
        else {
            to[key] = fVal;
        }
    });

    return to;
}

async function updateSubmission(formData) {    
    let updated = true
    let submission = await SubmissionModel.findById(formData.TrustServiceProvider.TSPID)       
    
    try {
        formData = submissionFormat.formToMongo(formData)    

        const updateTime = Date.now().toString()
        formData.ReviewInfo = {
            SubmittedDateTime: updateTime,
            ReviewStatus: networkSubmissionStatuses.PENDING,
            StatusStartDateTime: updateTime
        }
        formData.Submitter = {
            Username: submission.Submitter.Username,
            User_id: submission.Submitter.User_id
        }
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
            ReviewStatus: networkSubmissionStatuses.PENDING,
            StatusStartDateTime: submittedTime
        }

        try {
            let submission = new SubmissionModel({ 
                TrustServiceProvider: formData.TrustServiceProvider,
                Submitter: submitter, 
                ReviewInfo: reviewInfo            
            })

            submission.TrustServiceProvider.TSPID = submission._id
            formData.TrustServiceProvider.TSPID = submission._id
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
