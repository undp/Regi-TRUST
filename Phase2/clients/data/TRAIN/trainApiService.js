const config = require('../../Config/config.json').TRAIN
const axios = require('axios')

const trustListRoutes = config.baseUrl + config.trustListRoutes

const getTrustListVersions = async (token) => {
    const endpoint = trustListRoutes + config.trustList + config.trustListVersions;
    
    const trustListVersions = await axios.get(endpoint, {
        headers: { 'Authorization': 'Bearer ' + token }
    })    
        .then(res => res.data)
        .catch(err => {
            console.log(err);
            return null;
        });

    return trustListVersions
}
const getTrustList = async (version = null, token) => {
    const endpoint = trustListRoutes + config.trustList + config.trustListDetailed + `${version ? `?version=${version}` : ''}`
        
    const trustList = await axios.get(endpoint, {
        headers: { 'Authorization': 'Bearer ' + token }
    })
        .then(res => res.data)
        .catch(err => {
            console.log(err);
            return null;
        });

    return trustList
}

const getTspHistory = async (tspid = '', token) => {
    const endpoint = trustListRoutes + config.tsp + config.tspVersions + `/${tspid}`
    
    const tsp = await axios.get(endpoint, {
        headers: { 'Authorization': 'Bearer ' + token }
    })
        .then(res => res.data)
        .catch(err => {
            // console.log(err);
            return null;
        });

    return tsp
}
const getTspDetail = async (tspid, version = null,token) => {
    const endpoint = trustListRoutes + config.tsp + config.tspDetail + `${tspid}${version ? `?version=${version}` : ''}`
    
    const tspDetails = await axios.get(endpoint, {
        headers: { 'Authorization': 'Bearer ' + token }
    })     
        .then(res => res.data)
        .catch(err => {
            // console.log(err);
            return null;
        });

    return tspDetails
}

const postRegistryEntry = async (entry, token, isEdit = false) => {
    const endpoint = trustListRoutes + config.tsp + config.trustListDetailed + `${isEdit ? '/'+entry.TrustServiceProvider.TSPID: ''}`;

    const options = {
        method: isEdit ? 'PUT' : 'POST',
        headers: { 
            'Authorization': 'Bearer ' + token.trim(),
            'Content-Type': 'application/json'
        },
        data: JSON.stringify(entry.TrustServiceProvider),
        url: endpoint
    }

    const response = await axios(options)
        .catch(err => console.log(err))

    return response
}

const putFrameworkEntry = async (entry, token) => {
    const endpoint = trustListRoutes + config.trustList + config.trustFramework;

    const options = {
        method: 'PUT',
        headers: { 
            'Authorization': 'Bearer ' + token.trim(),
            'Content-Type': 'application/json'
        },
        data: JSON.stringify(entry),
        url: endpoint
    }

    const response = await axios(options)
        .then(res => res.data)
        .catch(err => console.log(err))

    return response
}

module.exports = { getTrustList, getTspDetail, postRegistryEntry, getTrustListVersions, getTspHistory, putFrameworkEntry }