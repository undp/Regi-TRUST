const mongoose = require('mongoose')
const {hostname, PORT, dbName, options} = require('../../Config/config.json').MongoDB
const SubmissionModel = require('./TSPSchema')
const Schema = mongoose.Schema

const initMongo = async () => {
    await mongoose.connect(`mongodb://${hostname}:${PORT}/${dbName}`, options)

    return mongoose.connection
}

const enrollSchema = new Schema(require('./TSPRepSchema'))
const EnrollModel = mongoose.model('EnrollmentRequest', enrollSchema, 'EnrollmentRequests')

module.exports = { initMongo, SubmissionModel , EnrollModel} 
