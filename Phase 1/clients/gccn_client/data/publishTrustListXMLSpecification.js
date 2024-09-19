'use strict'
var fs = require("fs")
var xml2js = require('xml2js')
var parser = new xml2js.Parser()

module.exports = new Promise((resolve, reject) => {
    fs.readFile('./data/publishTrustListXMLSpecification.xml', function (err, data) {
        if (err) reject('Error reading XML file.')
        parser.parseStringPromise(data)
            .then(function (result) {
                resolve(JSON.stringify(result))
            })
            .catch(function (err) {
                reject(err)
            })
    })
})