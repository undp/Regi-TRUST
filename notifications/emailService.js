const nodemailer = require('nodemailer')
const smtp = require('../config/config.json').Notifications
const pug = require('pug')
const { getReviewerEmails } = require('../auth/keycloak')

const templatePath = './notifications/emailTemplates/'
const linkBaseUrl = process.env.DNS || `http://localhost:${process.env.PORT}/`
const reviewLink = linkBaseUrl + 'review-submissions/submission/'


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

    else console.log(info)
}

const notifyNewSubmission = async (entityName, submissionId) => {
    const submissionReviewLink = reviewLink + submissionId
    const html = pug.renderFile(templatePath + 'receivedSubmission.pug', { entityName, submissionReviewLink })

    const reviewerEmails = await getReviewerEmails()
    reviewerEmails.forEach(toAddr => {
        const message = {
            from: 'notify@gccn.org',
            to: toAddr,
            subject: `New Submission from ${entityName}`,
            html
        }

        transport.sendMail(message, mailCallback)
    })
}

const notifySubmissionUpdated = (submitterEmail, entityName, submissionId) => {
    const submissionReviewLink = reviewLink + submissionId
    const html = pug.renderFile(templatePath + 'submissionUpdated.pug', { entityName, submissionReviewLink })

    const message = {
        from: 'notify@gccn.org',
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
        from: 'notify@gccn.org',
        to: submitterEmail,
        subject: `A Reviewer has ${reviewStatus} Your Submission`,
        html
    }

    transport.sendMail(message, mailCallback)
}

module.exports = { notifyNewSubmission, notifySubmissionUpdated, notifySubmissionReviewed }