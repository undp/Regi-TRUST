var OidcHelper = require('openid-client-helper')
var jwt = require('jsonwebtoken')
const keycloak = require('../Config/keycloak.json')
const client = require('../Config/config.json').Client
const  axios = require('axios')
const qs = require('qs')
const roleNames = require('../Config/config.json').roleNames

const realmUrl = keycloak['auth-server-url'] + 'realms/' + keycloak['realm']
const adminUrl = keycloak['auth-server-url'] + 'admin/realms/' + keycloak['realm']
const clientUrl = client.hostname

const issuerMetadata = {
    issuer: realmUrl,
    authorization_endpoint: realmUrl + '/protocol/openid-connect/auth',
    token_endpoint: realmUrl + '/protocol/openid-connect/token',
    revocation_endpoint: realmUrl + '/protocol/openid-connect/revoke'
}

const clientMetadata = {
    client_id: keycloak['resource'],
    client_secret: keycloak['credentials']['secret'],
    redirect_uri: clientUrl + 'auth/callback/',
    post_logout_redirect_uri: clientUrl
}

const serviceUserMetadata = {
    grant_type: 'client_credentials',
    client_id: keycloak['resource'],
    client_secret: keycloak['credentials']['secret']
}

const OidcHelperParams = {
    issuerMetadata: issuerMetadata,
    clientMetadata: clientMetadata,
    resources: {},
    useResourceIndicators: true,
    customize: ({
        custom,
        Issuer,
        issuer,
        client
    }) => {
        if (client)
            client[custom.clock_tolerance] = 1
    }
}

var {
    authorize,
    redirect,
    unauthorized,
    deauthorize,
    fetch,
    fetchMiddleware,
    getClient,
    getAuthorization,
    getClaims,
    getWWWAuthenticateHeaderAttributes
} = OidcHelper(OidcHelperParams)

const _getAuthTokenDecoded = (req) => {

    const userAuthz = req ? getAuthorization({ req }) : null

    return jwt.decode(userAuthz?.tokenSet?.access_token)
}

const getServiceUserToken = async (access_token) => {
    let options = {
        method: 'POST',
        url: issuerMetadata.token_endpoint,
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "Authorization": "Bearer " + access_token
        },
        data: qs.stringify(serviceUserMetadata)
    }

    let token = await axios(options)
        .then(res => res.data.access_token)
        .catch(err => console.error(err))

    return token;
};

const getAccessToken = (req) => {
    const userAuthz = req ? getAuthorization({ req }) : null;
    return userAuthz?.tokenSet?.access_token;
};

const getRoles = (req) => {
    const authToken = _getAuthTokenDecoded(req)

    return authToken?.resource_access[keycloak['resource']].roles || []
}

const getUserId = (req) => {
    const authToken = _getAuthTokenDecoded(req)

    return {
        Username: authToken?.preferred_username,
        User_id: authToken?.sub
    }
}

const checkAuthorized = (authorizedRoles) => {

    const returnFunction = (req, res, next) => {        
        if (!_getAuthTokenDecoded(req)) {
            req.session.returnTo = req.originalUrl
            res.redirect('/auth/')
        }

        else {
            const userRoles = getRoles(req)

            if(!authorizedRoles || userRoles.find(role => authorizedRoles.includes(role)))
                next()

            else {
                let err = new Error('Forbidden')
                err.status = 403
                next(err)
            }
        }
    }

    return returnFunction
}

const _getClientId = async (clientName, adminToken) => {
    let options = {
        method: 'GET',
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "Authorization": "Bearer " + adminToken
        },
        url: adminUrl + `/clients`
    }

    let clientId = await axios(options)
        .then(res => res.data.find(client => client.clientId === clientName).id)
        .catch(err => console.error(err))

    return clientId
}

const _getUsersByRole = async (role, clientId, adminToken) => {
    let options = {
        method: 'GET',
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "Authorization": "Bearer " + adminToken
        },
        url: adminUrl + `/clients/${clientId}/roles/${role}/users`
    }

    let users = await axios(options)
        .then(res => res.data)
        .catch(err => console.error(err))

    return users
}
const _getUserById = async (id, adminToken) => {
    let options = {
        method: 'GET',
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "Authorization": "Bearer " + adminToken
        },
        url: adminUrl + `/users/${id}`
    }

    let users = await axios(options)
        .then(res => res.data)
        .catch(err => console.error(err))

    return users
}

const getReviewerEmails = async (reviewer_type = roleNames.REVIEWER, serviceUserToken) => {
    let clientId = await _getClientId(clientMetadata.client_id, serviceUserToken);    
    let users = await _getUsersByRole(reviewer_type, clientId, serviceUserToken)

    return users?.filter(user => user.email).map(user => user.email)
}

const getSubmitterEmail = async (submitterId, serviceUserToken) => {
    let user = await _getUserById(submitterId, serviceUserToken)

    console.log(user)

    return user.email;
}

module.exports = {
    authorize,
    redirect,
    checkAuthorized,
    deauthorize,
    fetch,
    fetchMiddleware,
    getClient,
    getAuthorization,
    getAccessToken,
    getServiceUserToken,
    getClaims,
    getWWWAuthenticateHeaderAttributes,
    getReviewerEmails,
    getSubmitterEmail,
    getRoles,
    getUserId
}
