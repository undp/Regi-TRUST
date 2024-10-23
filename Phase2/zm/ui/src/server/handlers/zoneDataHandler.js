const needle = require('needle')
const { generators } = require('openid-client')
const codeVerifier = generators.codeVerifier()
async function getZoneData(req, res) {
  try {
    const accessToken = req.session.accessToken
    const url = `${process.env.ZONEMGR_URL}/view-zone`
    const zoneData = await needle('get',url, {
      headers: {
        authorization: `Bearer ${accessToken}`
      },
      json: true
    })
    if (zoneData.statusCode !== 200) {
      res.status(500).send('Error: ' + JSON.stringify(zoneData.body))
    } else {
      res.json(zoneData.body)    
    } 
  } catch (error) {
    res.status(500).send('Error: ' + error.message)
    console.log(`ERROR in /auth/login -> 
    MSG: ${error.message}
    STACK: ${error.stack}`)
  }
}

module.exports = {
  getZoneData
}