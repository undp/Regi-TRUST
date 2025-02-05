var SubmissionModel = require('../../data/MongoDB/mongoose').SubmissionModel;

const formatDate = (date) => {
    const options = { 
        year: 'numeric', 
        month: '2-digit', 
        day: '2-digit', 
        hour: '2-digit', 
        minute: '2-digit', 
        hour12: true 
    };
    const formatted = new Intl.DateTimeFormat('en-US', options).format(date);
    const [datePart, timePart] = formatted.split(', ');
    return `${datePart} ${timePart}`;
};

const formToMongo = (submission) => {
    submission.TrustServiceProvider.TSPServices?.TSPService.forEach(service => {
        if (Array.isArray(service.ServiceInformation.AdditionalServiceInformation.ServiceIssuedCredentialTypes.CredentialType)) {
            service.ServiceInformation.AdditionalServiceInformation.ServiceIssuedCredentialTypes.CredentialType = 
                service.ServiceInformation.AdditionalServiceInformation.ServiceIssuedCredentialTypes.CredentialType.join(', ')
        }
    });

    return submission
}


//Transforms necessary properties in a MongoDB Submission document to match the form fields' names
// for cases where Submission schema and form naming must diverge. Allows form pre-filling.
const mongoToForm = (submission) => {
    submission = submission.toJSON()

    submission.TSPInformation.TSPEntityIdentifierList = 
            submission.TSPInformation.TSPEntityIdentifierList.map(item => item.TSPEntityIdentifier)

    submission.TSPInformation.TSPCertificationList = 
        submission.TSPInformation.TSPCertificationList.map(item => item.TSPCertification)

    submission.TSPServices.forEach(service => {
        if(service.TSPService[0].ServiceInformation.AdditionalServiceInformation.ServiceIssuedCredentialTypes.length)
            service.TSPService[0].ServiceInformation.AdditionalServiceInformation.ServiceIssuedCredentialTypes = 
                service.TSPService[0].ServiceInformation.AdditionalServiceInformation.ServiceIssuedCredentialTypes.map(item => item.CredentialType)
    })

    return submission
}

//Returns objects for each section of the review page
//Each key and value formatted as labels and values as they will appear on screen
// Object format:
//   { Formatted Field Label : Formatted Field Value }
const mongoToReview = async (submission, formType = "network") => {
    //Review Information section
    let ReviewInfo = submission.ReviewInfo
    if(!ReviewInfo) {
        let localSubmission = await SubmissionModel.findOne({ "TrustServiceProvider.TSPID": submission.TSPID }).select('ReviewInfo')
        ReviewInfo = localSubmission.ReviewInfo
    }

    let SubmitterInfo = submission.Submitter
    if(formType !== "enroll"){
        if(!SubmitterInfo) {
            let localSubmission = await SubmissionModel.findOne({ "TrustServiceProvider.TSPID": submission.TSPID }).select('Submitter')
            SubmitterInfo = localSubmission.Submitter
        }
    }

    return Object.assign(jsonToDetailPage(submission, formType), { ReviewInfo, SubmitterInfo })
}

const jsonToDetailPage = (json, formType = "network") => {
    const _id = json && json._id ? json._id : json.TSPID ? json.TSPID : null;   

    switch(formType) {
        case "framework":
            json = json.FrameworkInformation;
            return {
                "Trust List Version": json.TSLVersion,
                "Trust List Type": json.TSLType,
                // "ListIssueDateTime": formatDate(new Date(json.ListIssueDateTime)),
                // "NextUpdate": formatDate(new Date(json.NextUpdate)),
                "Framework Name": json.FrameworkName.Name,
                "Operator Name": json.FrameworkOperatorName.Name,
                "Operator Address": Object.values(json.FrameworkOperatorAddress.PostalAddresses.PostalAddress[0]).join(', '),
                "Scheme Information URI": json.FrameworkInformationURI.URI,
                "Community Rules URI": json.FrameworkTypeCommunityRules.URI,
                "Territory": json.SchemeTerritory,
                "Policy or Legal Notice": json.PolicyOrLegalNotice.TSLLegalNotice,
                "Distribution Points": json.DistributionPoints.URI,
                "Extensions (TBD)": json.SchemeExtensions.URI,
                "Pointers to any Other Trust List": json.PointersToOtherTSL.map(pointer => pointer.URI).join(', '),
            }
        case "enroll":
            json = json.TrustServiceProvider;
            let submitterInfo = json.SubmitterInfo.toJSON();           
    
            const EnrollmentRequest = Object.assign({
                "Name:": submitterInfo.GivenName || '' + submitterInfo.SurName || '',
                "Agency Name:": submitterInfo.AgencyName,  
                "Job Title:": submitterInfo.JobTitle,      
                "Phone:": submitterInfo.Address?.PhoneNumber,
                "Electronic Address:": submitterInfo.Address?.ElectronicAddress,
                "Street Address:": [submitterInfo.Address?.PostalAddress.StreetAddress1, submitterInfo.Address?.PostalAddress.StreetAddress2].filter(addr => addr !== '').join(', '),
                "City:": submitterInfo.Address?.PostalAddress.City,
                "State/Province:": submitterInfo.Address?.PostalAddress.State,
                "Postal Code:": submitterInfo.Address?.PostalAddress.PostalCode,
                "Country:": submitterInfo.Address?.PostalAddress.Country,
                "Reason/Notes:": submitterInfo.Notes 
            })
            return {EnrollmentRequest, _id}
        case "network":          
            if(json.TrustServiceProvider)
                json = json.TrustServiceProvider;  

            //Reused address fields
            const buildAddressViewFormat = (address) => {
                const postalAddress = address.PostalAddresses[0]

                return {
                    "Electronic Address:": address.ElectronicAddress.URI,
                    "Street Address:": [postalAddress.StreetAddress1, postalAddress.StreetAddress2].filter(addr => addr !== '').join(', '),
                    "City:": postalAddress.Locality,
                    "State/Province:": postalAddress.State,
                    "Postal Code:": postalAddress.PostalCode,
                    "Country:": postalAddress.CountryName
                }
            }

            //Participating Entity Information - Organization Details section
            const TSPInfo = json.TSPInformation
            const TSPInformation = TSPInfo ? {
                "Name:": TSPInfo.TSPName.Name,
                "Legal Name:": TSPInfo.TSPLegalName.Name,
                "Role:": TSPInfo.TSPRole,
                "Legal Basis:": TSPInfo.TSPLegalBasis,
                "Information URL:": TSPInfo.TSPInformationURI.URI,
                "Entity Identifiers:": TSPInfo.TSPEntityIdentifierList.TSPEntityIdentifier.map(identifier => `${identifier.Type}: ${identifier.Value}`).join(', '),
                "Certifications:": TSPInfo.TSPCertificationLists.TSPCertification.map(cert => `${cert.Type}: ${cert.Value}`).join(', '),
                "Keywords:": TSPInfo.TSPKeywords    //TODO: not being returned
            } : {};    

            //Participating Entity Information - Organization Address section
            const TSPContacts = TSPInfo ? buildAddressViewFormat(TSPInfo.TSPAddress) : null;

            //Services - Details section
            const TSPServices = json.TSPServices ? json.TSPServices.TSPService.map(service => {
                service = service.ServiceInformation;
                
                return {
                    "Name:": service.ServiceName.Name,
                    "Service Type:": service.ServiceTypeIdentifier,
                    "Issued Certificate Types:": service.AdditionalServiceInformation.ServiceIssuedCredentialTypes.CredentialType.split(', ').filter(item => item.trim() !== ''),
                    "Digital Identity:": service.ServiceDigitalIdentity.DigitalId.Value,
                    "Supply Endpoint:": service.ServiceSupplyPoint,
                    "Definition URI:": service.ServiceDefinitionURI,
                    "Governance URI:": service.AdditionalServiceInformation.ServiceGovernanceURI,
                    "Business Rules URI:": service.AdditionalServiceInformation.ServiceBusinessRulesURI
                }
            }) : {};

            //Services - Service Operations Agent section
            const ServiceOpsAgents = json.TSPServices?.TSPService.map(service => {
                let opsAgent = service.OpsAgentInfo

                return Object.assign( { "Name:": opsAgent.OpsAgentName.Name }, buildAddressViewFormat(opsAgent.OpsAgentAddress))
            })            
            
            json = json.SubmitterInfo

            let Submitter = Object.assign( { "Name:": json.SubmitterName.Name }, buildAddressViewFormat(json.SubmitterAddress))

            return { TSPInformation, TSPContacts, TSPServices, ServiceOpsAgents, Submitter, _id }
    }
}

module.exports = { formToMongo, mongoToForm, jsonToDetailPage, mongoToReview, }
