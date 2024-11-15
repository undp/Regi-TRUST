const express = require('express')
const buildApp = require('./router')
const ViteExpress = require('vite-express')

const app = express()
buildApp(app).then((server) => {
  ViteExpress.config( {
    inlineViteConfig: {
      base: process.env.APP_BASE_URL_PATH === null || '/'
    }
  })
  ViteExpress.bind(app, server)
}).catch((error) => {
  console.log('Error starting server!: ' + error)
})


