'use strict';
var selectLists = require('./selectLists');

const addressFormComponent = (Addressee) => {
    return {
        "Type": "Wrapper",
        "Key": "Address",
        "Components": [
            {
                "Type": "Text",
                "SubType": "Email",
                "Key": "ElectronicAddress",
                "Label": Addressee + " Email",
                "Tooltip": Addressee +" email."
            },
            {
                "Type": "Wrapper",
                "Key": "PostalAddress",
                "Components": [
                    {
                        "Type": "Text",
                        "SubType": "Text",
                        "Key": "StreetAddress1",
                        "Label": "Street Address 1"
                    },
                    {
                        "Type": "Text",
                        "SubType": "Text",
                        "Key": "StreetAddress2",
                        "Label": "Street Address 2"
                    },
                    {
                        "Type": "Text",
                        "SubType": "Text",
                        "Key": "City",
                        "Label": "City"
                    },
                    {
                        "Type": "Text",
                        "SubType": "Text",
                        "Key": "State",
                        "Label": "State/Province"
                    },
                    {
                        "Type": "Text",
                        "SubType": "Text",
                        "Key": "PostalCode",
                        "Label": "Zip Code/Postal Code"
                    },
                    {
                        "Type": "Select",
                        "Key": "Country",
                        "Label": "Country",
                        "Values": selectLists.CountryList.map(country => ({"Value": country, "Label": country}))
                    }
                ]
            }
        ]
    }
}

const tsp = "TrustServiceProvider"
const tspInfo = tsp + "[TSPInformation]"
const tspSubmitter = tsp + "[SubmitterInfo]"
const tspService = tsp + "[TSPServices][0][TSPService]"

module.exports = {
    "FormSections": [
        {
            "SectionName": "Participating Entity Information",
            "Fields": [
                {
                    "Type": "Wrapper",
                    "Key": tspInfo,
                    "Components": [
                        {
                            "Type": "Text",
                            "SubType": "Text",
                            "Key": "TSPName",
                            "Label": "Entity Name",
                            "Tooltip": "Participating organization's operating or trade name."
                        },
                        {
                            "Type": "Text",
                            "SubType": "Text",
                            "Key": "TSPLegalName",
                            "Label": "Entity Legal Name",
                            "Tooltip": "Participating organization's legal name."
                        },
                        {
                            "Type": "Radio",
                            "Key":  "TSPRole",
                            "Label": "Entity Role",
                            "Values": [
                                {
                                    "Label": "Issuer (TSP)",
                                    "Value": "Issuer"
                                },
                                {
                                    "Label": "Registry Administrator (TSPA)",
                                    "Value": "Registry Administrator"
                                }
                            ],
                            "Tooltip": "Select the role that best applies to the participating entity/organization."
                        },
                        {
                            "Type": "Text",
                            "SubType": "Text",
                            "Key": "TrustSchemeName",
                            "Label": "Entity Trust Scheme Name"
                        },
                        {
                            "Type": "Text",
                            "SubType": "Text",
                            "Key": "OtherTSL",
                            "Label": "Other Trust Service List"
                        },
                        {
                            "Type": "Text",
                            "SubType": "URL",
                            "Key": "TSPInformationURI",
                            "Label": "Entity Information URL",
                            "Tooltip": "Participating organization's website URL."
                        },
                        {
                            "Type": "Text",
                            "SubType": "Text",
                            "Key": "TSPLegalBasis",
                            "Label": "Entity Legal Basis",
                            "Tooltip": "The legal basis for the entity - it may be dependent on the jurisdiction."
                        },
                        {
                            "Type": "EditGrid",
                            "Key": "TSPEntityIdentifierList",
                            "Label": "Entity Identifiers",
                            "EntityLabel": "Identifier",
                            "Components": [
                                {
                                    "Type": "Text",
                                    "SubType": "Text",
                                    "Key": "Type",
                                    "Label" : "Type"
                                },
                                {
                                    "Type": "Text",
                                    "SubType": "Text",
                                    "Key": "Value",
                                    "Label": "Value"
                                }
                            ],
                            "Tooltip": "Legal identifiers for the organization such as GLEIF, Registration/License numbers etc., comma separated."
                        },
                        {
                            "Type": "EditGrid",
                            "Key": "TSPCertificationList",
                            "Label": "Entity Certifications",
                            "EntityLabel": "Certification",
                            "Components": [
                                {
                                    "Type": "Text",
                                    "SubType": "Text",
                                    "Key": "Type",
                                    "Label" : "Type"
                                },
                                {
                                    "Type": "Text",
                                    "SubType": "Text",
                                    "Key": "Value",
                                    "Label": "Value"
                                }
                            ],
                            "Tooltip": "Jurisdiction or Industry compliance certifications, e.g., ISO, NIST certifications for a company."
                        },
                        {
                            "Type": "Text",
                            "SubType": "Text",
                            "Key": "TSPKeywords",
                            "Label": "Keywords",
                            "Tooltip": "Keywords or meta-tags for the participating organization and/or the trust registry service provided by them."
                        },

                        addressFormComponent("Entity")                        
                    ]
                }
            ]            
        },
        {
            "SectionName": "Submitter Contact Information",
            "Fields": [
                {
                    "Type": "Wrapper",
                    "Key": tspSubmitter,
                    "Components": [
                        {
                            "Type": "Text",
                            "SubType": "Text",
                            "Key": "Name",
                            "Label": "Submitter Name",
                            "Tooltip": "Provide the name of the individual / party submitting this information for GCCN entry."
                        },

                        addressFormComponent("Submitter")
                    ]
                }
            ]
        },
        {
            "SectionName": "Service Information",
            "Fields": [
                {
                    "Type": "Wrapper",
                    "Key": tspService,
                    "Components": [
                        {
                            "Type": "Hidden",
                            "Key": "ServiceCurrentStatus",
                            "Value": "new"
                        },
                        {
                            "Type": "Hidden",
                            "Key": "StatusStartingTime",
                            "Value": Date.now().toString()
                        },
                        {
                            "Type": "Text",
                            "SubType": "Text",
                            "Key": "ServiceName",
                            "Label": "Service Name",
                            "Tooltip": "Service's legal name."
                        },
                        {
                            "Type": "Radio",
                            "Key": "ServiceTypeIdentifier",
                            "Label": "Service Type",
                            "Values": [
                                {
                                    "Label": "Vaccination",
                                    "Value": "Vaccination"
                                },
                                {
                                    "Label": "Test",
                                    "Value": "Test"
                                },
                                {
                                    "Label": "Recovery",
                                    "Value": "Recovery"
                                },
                                {
                                    "Label": "Exemption",
                                    "Value": "Exemption"
                                },
                                {
                                    "Label": "Travel Pass",
                                    "Value": "Travel Pass"
                                }
                            ],
                            "Tooltip": "Select the types of credentials issued by the issuers listed in the trust registry."
                        },
                        {
                            "Type": "Wrapper",
                            "Key": "AdditionalServiceInformation",
                            "Components": [
                                {
                                    "Type": "CheckBox",
                                    "Key": "ServiceIssuedCredentialTypes",
                                    "Label": "Service Issued Credential Types",
                                    "Values": [
                                        {
                                            "Label": "Smart Health Card",
                                            "Value": "SHC"
                                        },
                                        {
                                            "Label": "etc.",
                                            "Value": "ETC"
                                        },
                                    ],
                                    "Tooltip": "Set of public keys, X509 certificates, URLs or technical format used."
                                },
                            ]
                        },
                        {
                            "Type": "Text",
                            "SubType": "URL",
                            "Key": "ServiceSupplyPoint",
                            "Label": "Service Supply Endpoint",
                            "Tooltip": "Service endpoint for the trust registry service."
                        },
                        {
                            "Type": "Text",
                            "SubType": "URL",
                            "Key": "ServiceDefinitionURI",
                            "Label": "Service Definition URI",
                            "Tooltip": "Additional information about the trust registry/service being provided at the service endpoint."
                        },
                        {
                            "Type": "InlineFields",
                            "Label": "Service Digital Identity",
                            "Key": "ServiceDigitalIdentity",
                            "Components": [
                                {
                                    "Type": "Text",
                                    "SubType": "Text",
                                    "Key": "Value",
                                    "Label": "Value"
                                },
                                {
                                    "Type": "Select",
                                    "Key": "KeyType",
                                    "Label": "Trust Type",
                                    "Values": [
                                        {
                                            "Label": "DID",
                                            "Value": "DID"
                                        },
                                        {
                                            "Label": "x509 Certificate",
                                            "Value": "x509 Certificate"
                                        }
                                    ]
                                },
                            ],
                            "Tooltip": "Provide the root of trust for the service listed - may be in x509, DID formats."
                        },                        
                        {
                            "Type": "Wrapper",
                            "Key": "AdditionalServiceInformation",
                            "Components": [
                                {
                                    "Type": "Text",
                                    "SubType": "URL",
                                    "Key": "ServiceGovernanceURI",
                                    "Label": "Service Governance URI",
                                    "Tooltip": "Applicable Governance Framework for the trust registry service."
                                },
                                {
                                    "Type": "Text",
                                    "SubType": "URL",
                                    "Key": "ServiceBusinessRulesURI",
                                    "Label": "Service Business Rules URI"
                                }
                            ],
                        }
                    ]
                }
            ]
        },
        {
            "SectionName": "Service Operational Contact Information",
            "Fields": [
                {
                    "Type": "Wrapper",
                    "Key": tspService + "[OpsAgent]",
                    "Components": [
                        {
                            "Type": "Text",
                            "SubType": "Text",
                            "Key": "Name",
                            "Label": "Ops Contact Name",
                            "Tooltip": "Ops Contact name."
                        },
                        addressFormComponent("Ops Contact")
                    ]
                }
            ]
        },
        {
            "SectionName": "Complete",
            "Fields": []
        }
    ]
}
