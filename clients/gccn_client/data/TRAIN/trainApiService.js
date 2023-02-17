const config = require('../../Config/config.json').TRAIN
const axios = require('axios')
const { json } = require('body-parser')

const getBearerToken = async () => {
    const endpoint = config.baseUrl + config.tokenEndpoint
    const body = {
        'grant_type': 'password',
        'client_id': config.client_id,
        'client_secret': config.client_secret,
        'scope': 'openid',
        'username': config.user,
        'password': config.pass
    }

    const options = {
        method: 'POST',
        headers: { 'content-type': 'application/x-www-form-urlencoded' },
        data: body,
        url: endpoint
    }

    const token = await axios(options)
        .then(response => response.data.access_token)

    return token
}

const trustListRoutes = config.baseUrl + config.trustListRoutes

const getTrustList = async () => {
    const endpoint = trustListRoutes + config.trustListDetailed + config.clientTrustScheme

    const trustList = await axios.get(endpoint)
        .then(response => response.data.TrustedServiceProviderDetails)

    return trustList
}

const getTspDetail = async (uid) => {
    const endpoint = trustListRoutes + config.tspDetail + config.clientTrustScheme + '/' + uid

    const tsp = await axios.get(endpoint)
        .then(response => response.data)

    return tsp
}

const putRegistryEntry = async (entry) => {
    const token = await getBearerToken()

    const endpoint = trustListRoutes + config.publishTrustList + config.clientTrustScheme
    const options = {
        method: 'PUT',
        headers: { 'Authorization': 'bearer ' + token },
        data: { TrustServiceProvider: entry.toJSON().TrustServiceProvider },
        url: endpoint
    }

    const response = await axios(options)
        .catch(err => false)

    return response
}



module.exports = { getTrustList, getTspDetail, putRegistryEntry }