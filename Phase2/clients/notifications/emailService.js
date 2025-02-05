const nodemailer = require('nodemailer')
const smtp = require('../Config/config.json').Notifications
const client = require('../Config/config.json').Client
const pug = require('pug')
const { getReviewerEmails, getSubmitterEmail, getServiceUserToken } = require('../Auth/keycloak')
const fs = require('fs')
const { roleNames, networkSubmissionStatuses } = require('../Config/config.json')

const templatePath = './notifications/emailTemplates/'
const linkBaseUrl = client.hostname
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
        logNotification(info.envelope.to[0])
    }
}

const logNotification = (recipient) => {
    const line = `\n${new Date().toLocaleString()} -- Email sent to ${recipient}`
    
    fs.mkdir('./logs', { recursive: true }, (err) => {
        if (err) return console.error('Error creating log directory:', err);

        fs.appendFile('./logs/emailNotifLog.txt', line, { flag: 'a' }, (err) => {
            if (err) console.error('Error writing to log:', err);
        });
    });
}

const notifyNewSubmission = async (entityName, submissionId, accessToken) => {
    const submissionReviewLink = reviewLink + submissionId
    const html = pug.renderFile(templatePath + 'receivedSubmission.pug', { entityName, submissionReviewLink })

    const reviewerEmails = await getReviewerEmails(undefined, accessToken)
    const message = {
        from: smtp.username,
        subject: `[RegiTrust] New Submission from ${entityName}`,
        html
    }
    for (const toAddr of reviewerEmails) {
        message.to = toAddr;
        try {
            await new Promise((resolve, reject) => {
                transport.sendMail(message, (error, info) => {
                    mailCallback(error, info);

                    if (error) return reject(error);
                    resolve(info);
                });
            });
        } catch (error) {
            console.error("Error sending email to", toAddr, error);
        }
    }
}

const notifyNewEnrollmentRequest = async (submitterEmail, entityName, submissionId, accessToken) => {
    const submissionReviewLink = reviewEnrollmentLink + submissionId
    html = pug.renderFile(templatePath + 'receivedEnrollmentRequest.pug', { entityName, submissionReviewLink, isReviewer: true })

    accessToken = await getServiceUserToken()

    /********************message to reviewers********************/
    let message = {
        from: smtp.username,
        subject: `[RegiTrust] New Enrollment Request from ${entityName}`,
        html
    };

    const reviewerEmails = await getReviewerEmails(roleNames.ONBOARDING_MANAGER, accessToken);
    for (const toAddr of reviewerEmails) {
        message.to = toAddr;
        try {
            await new Promise((resolve, reject) => {
                transport.sendMail(message, (error, info) => {
                    mailCallback(error, info);

                    if (error) return reject(error);
                    resolve(info);
                });
            });
        } catch (error) {
            console.error("Error sending email to", toAddr, error);
        }
    }

    /********************message to submitter********************/
    message.to = submitterEmail,
    message.subject = `[RegiTrust] Enrollment Request from ${entityName} Confirmation`,
    message.html = pug.renderFile(templatePath + 'receivedEnrollmentRequest.pug', { entityName, submissionReviewLink, isReviewer: false });

    transport.sendMail(message, mailCallback)
}

const notifySubmissionUpdated = async (submitterId, entityName, submissionId, isSubmitterEdit) => {
    const submissionReviewLink = reviewLink + submissionId
    const html = pug.renderFile(templatePath + 'submissionUpdated.pug', { entityName, submissionReviewLink, isSubmitterEdit })

    const accessToken = await getServiceUserToken()
    const submitterEmail = await getSubmitterEmail(submitterId, accessToken)

    const message = {
        from: smtp.username,
        to: submitterEmail,
        html
    }

    if(!isSubmitterEdit) {
        message.subject = `[RegiTrust] A Reviewer has Made Changes to Your Submission`;
        transport.sendMail(message, mailCallback)
    }else{
        message.subject = `[RegiTrust] A Submission has been Updated`
        const accessToken = await getServiceUserToken()
        const reviewerEmails = await getReviewerEmails(roleNames.REVIEWER, accessToken)
        for (const toAddr of reviewerEmails) {
            message.to = toAddr;
            try {
                await new Promise((resolve, reject) => {
                    transport.sendMail(message, (error, info) => {
                        mailCallback(error, info);

                        if (error) return reject(error);
                        resolve(info);
                    });
                });
            } catch (error) {
                console.error("Error sending email to", toAddr, error);
            }
        }
    }
}

const notifySubmissionReviewed = async (submitterId, entityName, reviewStatus, submissionId) => {
    const submissionReviewLink = reviewLink + submissionId
    const html = pug.renderFile(templatePath + 'submissionReviewed.pug', { entityName, isApproved: reviewStatus == networkSubmissionStatuses.APPROVED, submissionReviewLink })
    
    const accessToken = await getServiceUserToken()
    const submitterEmail = await getSubmitterEmail(submitterId, accessToken)

    const message = {
        from: smtp.username,
        to: submitterEmail,
        subject: `[RegiTrust] A Reviewer has ${reviewStatus} Your Submission`,
        html
    }

    transport.sendMail(message, mailCallback)
}

const notifyEnrollmentRequestApproved = async (submitterEmail, entityName) => {
    html = pug.renderFile(templatePath + 'enrollmentRequestApproved.pug', { entityName })

    /**************Notify Submitter********************** */
    const message = {
        from: smtp.username,
        to: submitterEmail,
        subject: `[RegiTrust] Enrollment Request has Been Reviewed`,
        html
    }
    
    transport.sendMail(message, mailCallback)
}

module.exports = { notifyNewSubmission, notifyNewEnrollmentRequest, notifySubmissionUpdated, notifySubmissionReviewed, notifyEnrollmentRequestApproved }