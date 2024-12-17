'use strict';
var express = require('express');
var router = express.Router();

var formJSON = require('../../data/formFields/framework');
var { checkAuthorized, getRoles } = require('../../Auth/keycloak');

const trainApi = require('../../data/TRAIN/trainApiService');
var roles = require('../../Auth/roles');

/* GET enroll form page. */
router.get('/:step', checkAuthorized([roles.ADMIN]), async function (req, res) {
    let step = parseInt(req.params['step']);

    let roles = getRoles(req);

    let sessionData = null;    
    if (!sessionData) {
        req.session.sessionData = {
            formData: {validation: formJSON.FormSections.map(() => 'true').slice(1)}
        };
        sessionData = req.session.sessionData;
        step = 1
    }

    sessionData.formData.FrameworkInformation = await trainApi.getTrustList(null, req.session.accessToken)
        .then(res => res)
        .catch(err => console.log(err))

    req.session.sessionData.formData = sessionData.formData;

    if (step === formJSON.FormSections.length) {
        if (req.session.submitted) {
            req.session.sessionData = null;
            req.session.submitted = null;
        } else {
            return res.redirect('/gccn-framework/' + (formJSON.FormSections.length - 1));
        }
    }

    res.render('./forms/framework', {
        currentStep: step,
        validation: sessionData.formData.validation,
        formJSON: formJSON,
        formData: sessionData.formData.FrameworkInformation,
        title: 'Trust Service Framework',
        roles: roles
    });
});

/* Post TSP enrollmment request. */
router.post('/:step', checkAuthorized([roles.ADMIN]),  async function (req, res, next) {   
    let sessionData = req.session.sessionData;
    
    if (!sessionData) {
        sessionData = {
            formData: {}
        };

        req.session.sessionData = sessionData;
    }

    let formData = sessionData.formData.FrameworkInformation;     
    formData = mergeDeep(formData, req.body);

    formData.FrameworkInformation.ListIssueDateTime = new Date();
    if(formData.FrameworkInformation.PointersToOtherTSL[0].URI == '')
        formData.FrameworkInformation.PointersToOtherTSL[0].URI = null;

    req.session.submitted = await trainApi.putFrameworkEntry(formData, req.session.accessToken);

    if(req.session.submitted){
        res.redirect('/framework/details')
    }
});

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
