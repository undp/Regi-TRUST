const nodemailer = require('nodemailer')
const smtp = require('../Config/config.json').Notifications
const realmAdminEmail = require('../Config/keycloak.json').realmAdminEmail
const pug = require('pug')
const { getReviewerEmails } = require('../Auth/keycloak')
const fs = require('fs')

const templatePath = './notifications/emailTemplates/'
const linkBaseUrl = process.env.DNS || `http://localhost:${process.env.PORT}/`
const reviewLink = linkBaseUrl + 'review-submissions/submission/'
const reviewEnrollmentLink = linkBaseUrl + 'review-enrollment-requests/submission/'


const transport = nodemailer.createTransport({
    host: smtp.host,
    port: smtp.port,
    auth: {
      user: smtp.username,
      pass: smtp.password
    }
});

const mailCallback = (err, info) => {
    if(err)
        console.error(err)

    else {
        console.log(info)
        logNotification(info.envelope.to[0])
    }
}

const logNotification = (recipient) => {
    const line = `\n${new Date().toLocaleString()} -- Email sent to ${recipient}`
    
    fs.appendFile('./logs/emailNotifLog.txt', line, err => { if(err) console.error(err) })
}

const notifyNewSubmission = async (entityName, submissionId, accessToken) => {
    const submissionReviewLink = reviewLink + submissionId
    const html = pug.renderFile(templatePath + 'receivedSubmission.pug', { entityName, submissionReviewLink })

    const reviewerEmails = [realmAdminEmail]//await getReviewerEmails(null, accessToken)
    reviewerEmails?.forEach(toAddr => {
        const message = {
            from: 'svcemailservice@symsoftsolutions.com',
            to: toAddr,
            subject: `New Submission from ${entityName}`,
            html
        }

        transport.sendMail(message, mailCallback)
    })
}

const notifyNewEnrollmentRequest = async (submitterEmail, entityName, submissionId, accessToken) => {
    const submissionReviewLink = reviewEnrollmentLink + submissionId
    html = pug.renderFile(templatePath + 'receivedEnrollmentRequest.pug', { entityName, submissionReviewLink, isReviewer: true })

    /********************message to reviewers********************/
    let message = {
        from: 'svcemailservice@symsoftsolutions.com',
        subject: `New Enrollment Request from ${entityName}`,
        html
    };

    const reviewerEmails = [realmAdminEmail]//await getReviewerEmails("Onboarding_manager", accessToken)
    reviewerEmails?.forEach(toAddr => {
        message.to = toAddr;
        transport.sendMail(message, mailCallback)
    })

    /********************message to submitter********************/
    message.to = submitterEmail,
    message.subject = `Enrollment Request from ${entityName} Confirmation`,
    message.html = pug.renderFile(templatePath + 'receivedEnrollmentRequest.pug', { entityName, submissionReviewLink, isReviewer: false });

    transport.sendMail(message, mailCallback)
}

const notifySubmissionUpdated = (submitterEmail, entityName, submissionId) => {
    const submissionReviewLink = reviewLink + submissionId
    const html = pug.renderFile(templatePath + 'submissionUpdated.pug', { entityName, submissionReviewLink })

    const message = {
        from: 'svcemailservice@symsoftsolutions.com',
        to: submitterEmail,
        subject: 'A Reviewer has Made Changes to Your Submission',
        html
    }

    transport.sendMail(message, mailCallback)
}

const notifySubmissionReviewed = (submitterEmail, entityName, reviewStatus, submissionId) => {
    const submissionReviewLink = reviewLink + submissionId
    const html = pug.renderFile(templatePath + 'submissionReviewed.pug', { entityName, reviewStatus, submissionReviewLink })

    const message = {
        from: 'svcemailservice@symsoftsolutions.com',
        to: submitterEmail,
        subject: `A Reviewer has ${reviewStatus} Your Submission`,
        html
    }

    transport.sendMail(message, mailCallback)
}

const notifyEnrollmentRequestReviewed = async (submitterEmail, entityName, reviewStatus, submissionId) => {
    const submissionReviewLink = reviewEnrollmentLink + submissionId
    html = pug.renderFile(templatePath + 'enrollmentRequestReviewed.pug', { entityName, reviewStatus, submissionReviewLink, isAdmin: false })

    /**************Notify Submitter********************** */
    const message = {
        from: 'svcemailservice@symsoftsolutions.com',
        to: submitterEmail,
        subject: `Enrollment Request Reviewed`,
        html
    }

    transport.sendMail(message, mailCallback)
    
    /**************Notify Realm Administrator******************* */
    if(reviewStatus == "Approved"){
        message.to = realmAdminEmail
        message.subject = `A Reviewer has ${reviewStatus} an Enrollment Request`,
        message.html = pug.renderFile(templatePath + 'enrollmentRequestReviewed.pug', { entityName, reviewStatus, submissionReviewLink, isAdmin: true })
        
        transport.sendMail(message, mailCallback)
    }
}

module.exports = { notifyNewSubmission, notifyNewEnrollmentRequest, notifySubmissionUpdated, notifySubmissionReviewed, notifyEnrollmentRequestReviewed }