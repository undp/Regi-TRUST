//Transforms necessary properties in a form submission object to match the MongoDB submission schema
// for cases where Submission schema and form naming must diverge. Allows creation of a Submission 
// document following the correct schema.
const formToMongo = (formData) => {
    formData.TrustServiceProvider.TSPInformation.TSPEntityIdentifierList = 
            formData.TrustServiceProvider.TSPInformation.TSPEntityIdentifierList.map(item => ({TSPEntityIdentifier: item}))

    formData.TrustServiceProvider.TSPInformation.TSPCertificationList = 
        formData.TrustServiceProvider.TSPInformation.TSPCertificationList.map(item => ({TSPCertification: item}))

    formData.TrustServiceProvider.TSPServices.forEach(service => {
        service.TSPService.AdditionalServiceInformation.ServiceIssuedCredentialTypes = 
            service.TSPService.AdditionalServiceInformation.ServiceIssuedCredentialTypes
                .map(item => ( item ? {CredentialType: item} : null)).filter(item => item)
    })

    return formData
}

//Transforms necessary properties in a MongoDB Submission document to match the form fields' names
// for cases where Submission schema and form naming must diverge. Allows form pre-filling.
const mongoToForm = (submission) => {
    submission = submission.toJSON()

    submission.TrustServiceProvider.TSPInformation.TSPEntityIdentifierList = 
            submission.TrustServiceProvider.TSPInformation.TSPEntityIdentifierList.map(item => item.TSPEntityIdentifier)

    submission.TrustServiceProvider.TSPInformation.TSPCertificationList = 
        submission.TrustServiceProvider.TSPInformation.TSPCertificationList.map(item => item.TSPCertification)

    submission.TrustServiceProvider.TSPServices.forEach(service => {
        if(service.TSPService.AdditionalServiceInformation.ServiceIssuedCredentialTypes.length)
            service.TSPService.AdditionalServiceInformation.ServiceIssuedCredentialTypes = 
                service.TSPService.AdditionalServiceInformation.ServiceIssuedCredentialTypes.map(item => item.CredentialType)
    })

    return submission
}

//Returns objects for each section of the review page
//Each key and value formatted as labels and values as they will appear on screen
// Object format:
//   { Formatted Field Label : Formatted Field Value }
const mongoToReview = (submission) => {
    //Review Information section
    const ReviewInfo = submission.ReviewInfo

    return Object.assign(jsonToDetailPage(submission), { ReviewInfo })
}

const jsonToDetailPage = (json) => {
    const TSPInfo = json.TrustServiceProvider.TSPInformation

    //Reused address fields
    const buildAddressViewFormat = (address) => {
        const postalAddress = address.PostalAddress

        return {
            "Email:": address.ElectronicAddress,
            "Street Address:": [postalAddress.StreetAddress1, postalAddress.StreetAddress2].filter(addr => addr !== '').join(', '),
            "City:": postalAddress.City,
            "State/Province:": postalAddress.State,
            "Postal Code:": postalAddress.PostalCode,
            "Country:": postalAddress.Country
        }
    }
    
    //Participating Entity Information - Organization Details section
    const TSPInformation = {
        "Name:": TSPInfo.TSPName,
        "Legal Name:": TSPInfo.TSPLegalName,
        "Role:": TSPInfo.TSPRole,
        "Legal Basis:": TSPInfo.TSPLegalBasis,
        "Information URL:": TSPInfo.TSPInformationURI,
        "Entity Identifiers:": TSPInfo.TSPEntityIdentifierList?.map(identifier => `${identifier.TSPEntityIdentifier.Type}: ${identifier.TSPEntityIdentifier.Value}`).join(', '),
        "Certifications:": TSPInfo.TSPCertificationList?.map(cert => `${cert.TSPCertification.Type}: ${cert.TSPCertification.Value}`).join(', '),
        "Keywords:": TSPInfo.TSPKeywords
    }

    //Participating Entity Information - Organization Address section
    const TSPContacts = buildAddressViewFormat(TSPInfo.Address)

    //Services - Details section
    const TSPServices = json.TrustServiceProvider.TSPServices.map(service => {
        service = service.TSPService
        
        return {
            "Name:": service.ServiceName,
            "Service Type:": service.ServiceTypeIdentifier,
            "Issued Certificate Types:": service.AdditionalServiceInformation.ServiceIssuedCredentialTypes.map(cred => cred.CredentialType).join(', '),
            "Digital Identity:": service.ServiceDigitalIdentity.KeyType + ": " + service.ServiceDigitalIdentity.Value,
            "Supply Endpoint:": service.ServiceSupplyPoint,
            "Definition URI:": service.ServiceDefinitionURI,
            "Governance URI:": service.AdditionalServiceInformation.ServiceGovernanceURI,
            "Business Rules URI:": service.AdditionalServiceInformation.ServiceBusinessRulesURI
        }
    })

    //Services - Service Operations Agent section
    const ServiceOpsAgents = json.TrustServiceProvider.TSPServices.map(service => {
        let opsAgent = service.TSPService.OpsAgent

        return Object.assign( { "Name:": opsAgent.Name }, buildAddressViewFormat(opsAgent.Address))
    })

    //Submitter - Contact Information section
    const submitterInfo = json.TrustServiceProvider.SubmitterInfo
    const Submitter = Object.assign( { "Name:": submitterInfo.Name }, buildAddressViewFormat(submitterInfo.Address))

    const _id = json._id

    return { TSPInformation, TSPContacts, TSPServices, ServiceOpsAgents, Submitter, _id }
}

const validateApiResponse = (tsp) => {
    if(!Array.isArray(tsp.TrustServiceProvider.TSPInformation.TSPCertificationList))
    tsp.TrustServiceProvider.TSPInformation.TSPCertificationList = [tsp.TrustServiceProvider.TSPInformation.TSPCertificationList]

    if(!Array.isArray(tsp.TrustServiceProvider.TSPInformation.TSPEntityIdentifierList))
    tsp.TrustServiceProvider.TSPInformation.TSPEntityIdentifierList = [tsp.TrustServiceProvider.TSPInformation.TSPEntityIdentifierList]

    if(!Array.isArray(tsp.TrustServiceProvider.TSPServices))
        tsp.TrustServiceProvider.TSPServices = [tsp.TrustServiceProvider.TSPServices]

    tsp.TrustServiceProvider.TSPServices = tsp.TrustServiceProvider.TSPServices.map(service => {
        if(!Array.isArray(service.TSPService.AdditionalServiceInformation.ServiceIssuedCredentialTypes))
            service.TSPService.AdditionalServiceInformation.ServiceIssuedCredentialTypes = [service.TSPService.AdditionalServiceInformation.ServiceIssuedCredentialTypes]
        
        return service
    })

    return tsp
}

module.exports = { formToMongo, mongoToForm, jsonToDetailPage, mongoToReview, apiToView: validateApiResponse }
