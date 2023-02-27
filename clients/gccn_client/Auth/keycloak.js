var OidcHelper = require('openid-client-helper')
var jwt = require('jsonwebtoken')
const keycloak = require('../Config/keycloak.json')
const client = require('../Config/config.json').Client
const  axios = require('axios')

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

const _getAdminToken = async () => {
    let options = {
        method: 'POST',
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        data: {
            grant_type: "password",
            client_id: clientMetadata.client_id,
            client_secret: clientMetadata.client_secret,
            username: keycloak.admin.username,
            password: keycloak.admin.password
        },
        url: issuerMetadata.token_endpoint //keycloak['auth-server-url'] + 'realms/master/protocol/openid-connect/token'
    }

    let token = await axios(options)
        .then(res => res.data.access_token)
        .catch(err => console.error(err))

    return token
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

const getReviewerEmails = async () => {
    let adminToken = await _getAdminToken()
    let clientId = await _getClientId(clientMetadata.client_id, adminToken)
    let users = await _getUsersByRole('Registry_reviewer', clientId, adminToken)    

    return users.filter(user => user.email).map(user => user.email)
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
    getClaims,
    getWWWAuthenticateHeaderAttributes,
    getReviewerEmails,
    getRoles,
    getUserId,
}
