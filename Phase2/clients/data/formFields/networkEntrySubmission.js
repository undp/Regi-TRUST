'use strict';
var selectLists = require('../selectLists');

const addressFormComponent = (Addressee) => {
    return {
        "Type": "Wrapper",
        "Key": Addressee + "Address",
        "Components": [
            {
                "Type": "Wrapper",
                "Key": "ElectronicAddress",
                "Components": [
                    {
                        "Type": "Text",
                        "SubType": "Email",
                        "Key": "URI",
                        "Label": "Electronic Address",
                        "Validation": {
                            "Required": true
                        }
                    }
                ]
            },
            {
                "Type": "Wrapper",
                "Key": "PostalAddresses",
                "Components": [
                    {
                        "Type": "Wrapper",
                        "Key": "0",
                        "Components": [
                            {
                                "Type": "Text",
                                "SubType": "Text",
                                "Key": "StreetAddress1",
                                "Label": "Street Address 1",
                                "Validation": {
                                    "Required": true
                                }
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
                                "Key": "Locality",
                                "Label": "City",
                                "Validation": {
                                    "Required": true
                                }
                            },
                            {
                                "Type": "Text",
                                "SubType": "Text",
                                "Key": "State",
                                "Label": "State/Province",
                                "Validation": {
                                    "Required": true
                                }
                            },
                            {
                                "Type": "Text",
                                "SubType": "Text",
                                "Key": "PostalCode",
                                "Label": "Zip Code/Postal Code",
                                "Validation": {
                                    "Required": true
                                }
                            },
                            {
                                "Type": "Select",
                                "Key": "CountryName",
                                "Label": "Country",
                                "Values": selectLists.CountryList.map(country => ({"Value": country, "Label": country})),    //TODO: the country id from backend needs to match values in selectList.js
                                "Validation": {
                                    "Required": true
                                }
                            }
                        ]
                    }
                ]
            }
        ]
    }
}

const tsp = "TrustServiceProvider"
const tspInfo = tsp + "[TSPInformation]"
const tspSubmitter = tsp + "[SubmitterInfo]"
const tspService = tsp + "[TSPServices][TSPService][0]"

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
                            "Type": "Wrapper",
                            "Key": "TSPName",
                            "Components": [
                                {
                                    "Type": "Text",
                                    "SubType": "Text",
                                    "Key": "Name",
                                    "Label": "Entity Name",
                                    "Tooltip": "Participating organization's operating or trade name.",
                                    "Validation": {
                                        "Required": true
                                    }
                                }
                            ]
                        },
                        {
                            "Type": "Wrapper",
                            "Key": "TSPLegalName",
                            "Components": [
                                {
                                    "Type": "Text",
                                    "SubType": "Text",
                                    "Key": "Name",
                                    "Label": "Legal Name",
                                    "Tooltip": "Participating organization's legal name.",
                                    "Validation": {
                                        "Required": true
                                    }
                                }
                            ]
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
                            "Tooltip": "Select the role that best applies to the participating entity/organization.",
                            "Validation": {
                                "Required": true
                            }
                        },
                        {
                            "Type": "Text",
                            "SubType": "Text",
                            "Key": "TrustSchemeName",
                            "Label": "Entity Trust Scheme Name",
                            "Validation": {
                                "Required": true
                            }
                        },
                        {
                            "Type": "Text",
                            "SubType": "Text",
                            "Key": "OtherTSL",
                            "Label": "Other Trust Service List"
                        },
                        {
                            "Type": "Wrapper",
                            "Key": "TSPInformationURI",
                            "Components": [
                                {
                                    "Type": "Text",
                                    "SubType": "URL",
                                    "Key": "URI",
                                    "Label": "Entity Information URL",
                                    "Tooltip": "Participating organization's website URL.",
                                    "Validation": {
                                        "Required": true
                                    }
                                }
                            ]
                        },
                        {
                            "Type": "Text",
                            "SubType": "Text",
                            "Key": "TSPLegalBasis",
                            "Label": "Entity Legal Basis",
                            "Tooltip": "The legal basis for the entity - it may be dependent on the jurisdiction.",
                            "Validation": {
                                "Required": true
                            }
                        },
                        {
                            "Type": "Wrapper",
                            "Key": "TSPEntityIdentifierList",
                            "Components": [
                                {
                                    "Type": "EditGrid",
                                    "Key": "TSPEntityIdentifier",
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
                                }
                            ]
                        },
                        {
                            "Type": "Wrapper",
                            "Key": "TSPCertificationLists",
                            "Components": [
                                {
                                   "Type": "EditGrid",
                                    "Key": "TSPCertification",
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
                                }
                            ]
                        },
                        {
                            "Type": "Text",
                            "SubType": "Text",
                            "Key": "TSPKeywords",
                            "Label": "Keywords",
                            "Tooltip": "Keywords or meta-tags for the participating organization and/or the trust registry service provided by them."
                        },

                        addressFormComponent("TSP")                        
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
                            "Type": "Wrapper",
                            "Key": "SubmitterName",
                            "Components": [
                                {
                                    "Type": "Text",
                                    "SubType": "Text",
                                    "Key": "Name",
                                    "Label": "Submitter Name",
                                    "Tooltip": "Provide the name of the individual / party submitting this information for GCCN entry.",
                                    "Validation": {
                                        "Required": true
                                    }
                                }
                            ]
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
                    "Type": "Fieldset",
                    "Key": tspService,
                    "Label": "Service Information",
                    "Components": [
                        {
                            "Type": "Wrapper",
                            "Key": "ServiceInformation",
                            "Components": [
                                {
                                    "Type": "Hidden",
                                    "Key": "ServiceStatus",
                                    "Value": "https://kemkes.go.id/ServiceStatus/active"
                                },
                                {
                                    "Type": "Hidden",
                                    "Key": "StatusStartingTime",
                                    "Value": Date.now().toString()
                                },
                                {
                                    "Type": "Wrapper",
                                    "Key": "ServiceName",
                                    "Components": [
                                        {
                                            "Type": "Text",
                                            "SubType": "Text",
                                            "Key": "Name",
                                            "Label": "Service Name",
                                            "Tooltip": "Service's legal name.",
                                            "Validation": {
                                                "Required": true
                                            }
                                        }
                                    ],
                                    
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
                                    "Tooltip": "Select the types of credentials issued by the issuers listed in the trust registry.",
                                    "Validation": {
                                        "Required": true
                                    }
                                },
                                {
                                    "Type": "Wrapper",
                                    "Key": "AdditionalServiceInformation",
                                    "Components": [
                                        {
                                            "Type": "Wrapper",
                                            "Key": "ServiceIssuedCredentialTypes",
                                            "Components": [
                                                {
                                                    "Type": "CheckBox",
                                                    "Key": "CredentialType",
                                                    "Label": "Service Issued Credential Types",
                                                    "Values": [
                                                        {
                                                            "Label": "Smart Health Card",
                                                            "Value": "SHC"
                                                        },
                                                        {
                                                            "Label": "HAJ",
                                                            "Value": "HajCredential"
                                                        },
                                                        {
                                                            "Label": "etc.",
                                                            "Value": "ETC"
                                                        },
                                                    ],
                                                    "Tooltip": "Set of public keys, X509 certificates, URLs or technical format used.",
                                                    "Validation": {
                                                        "Required": true
                                                    }
                                                }
                                            ]
                                        },
                                    ]
                                },
                                {
                                    "Type": "Text",
                                    "SubType": "URL",
                                    "Key": "ServiceSupplyPoint",
                                    "Label": "Service Supply Endpoint",
                                    "Tooltip": "Service endpoint for the trust registry service.",
                                    "Validation": {
                                        "Required": true
                                    }
                                },
                                {
                                    "Type": "Text",
                                    "SubType": "URL",
                                    "Key": "ServiceDefinitionURI",  //TODO: fix typo in backend
                                    "Label": "Service Definition URI",
                                    "Tooltip": "Additional information about the trust registry/service being provided at the service endpoint.",
                                    "Validation": {
                                        "Required": true
                                    }
                                },
                                {
                                    "Type": "Wrapper",
                                    "Key": "ServiceDigitalIdentity",
                                    "Components": [
                                        {
                                            "Type": "InlineFields",
                                            "Label": "Service Digital Identity",
                                            "Key": "DigitalId",
                                            "Components": [
                                                {
                                                    "Type": "Text",
                                                    "SubType": "Text",
                                                    "Key": "Value",
                                                    "Label": "Value",
                                                    "Validation": {
                                                        "Required": true
                                                    }
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
                                                    ],
                                                    "Validation": {
                                                        "Required": true
                                                    }
                                                },
                                            ],
                                            "Tooltip": "Provide the root of trust for the service listed - may be in x509, DID formats."
                                        }
                                    ]
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
                    "Type": "Fieldset",
                    "Key": tspService,
                    "Label": "Service Ops Agent Information",
                    "Components": [
                        {
                    
                            "Type": "Wrapper",
                            "Key": "OpsAgentInfo",
                            "Components": [
                                {
                                    "Type": "Wrapper",
                                    "Key": "OpsAgentName",
                                    "Components": [
                                        {
                                            "Type": "Text",
                                            "SubType": "Text",
                                            "Key": "Name",
                                            "Label": "Ops Contact Name",
                                            "Tooltip": "Operational Contact name.",
                                            "Validation": {
                                                "Required": true
                                            }
                                        }
                                    ]
                                },
                                addressFormComponent("OpsAgent")
                            ]
                        }
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
