const mongoose = require('mongoose')
const {hostname, PORT, dbName, options} = require('../../Config/config.json').MongoDB
const Schema = mongoose.Schema

const initMongo = async () => {
    await mongoose.connect(`mongodb://${hostname}:${PORT}/${dbName}`, options)

    return mongoose.connection
}

const submissionSchema = new Schema(require('./TSPSchema'))
const SubmissionModel = mongoose.model('Submission', submissionSchema, 'Submissions')

module.exports = { initMongo, SubmissionModel } 
