const session = require('express-session')
const Store = require('connect-mongodb-session')(session)
const { hostname, PORT, dbName } = require('../../Config/config.json').MongoDB
const debug = require('debug')

const store = new Store({
    uri: `mongodb://${hostname}:${PORT}/${dbName}`,
    collection: 'Sessions'
})

store.on('error', function(error) {
    debug(error)
})

module.exports = store