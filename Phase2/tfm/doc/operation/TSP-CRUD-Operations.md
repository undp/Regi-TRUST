# Publish Trust-list 
# Example

# TSP publish
    Url: PUT -> "http://localhost:16003/tspa-service/tspa/v1/gxfs-prep.testtrain.trust-scheme.de/trust-list/tsp"

    request body: application/json

   
```
{
        "TrustServiceProvider": {
            "UUID": "8271fcbf-0622-4415-b8b1-34ad74215dc6",
            "TSPName": "CompanyaA Gmbh",
            "TSPTradeName": "CompanyaA Gmbh",
            "TSPInformation": {
                "Address": {
                    "ElectronicAddress": "info@companya.de",
                    "PostalAddress": {
                        "City": "Stuttgart",
                        "Country": "DE",
                        "PostalCode": "11111",
                        "State": "BW",
                        "StreetAddress1": "Hauptsr",
                        "StreetAddress2": "071"
                    }
                },
                "TSPCertificationList": {
                    "TSPCertification": [
                        {
                            "Type": "ISO:9001",
                            "Value": "4356546745"
                        },
                        {
                            "Type": "EU-VAT",
                            "Value": "4356546745"
                        }
                    ]
                },
                "TSPEntityIdentifierList": {
                    "TSPEntityIdendifier": [
                        {
                            "Type": "vLEI",
                            "Value": "3453654764"
                        },
                        {
                            "Type": "VAT",
                            "Value": "3453654764"
                        }
                    ]
                },
                "TSPInformationURI": "string"
            },
            "TSPServices": {
                "TSPService": [
                    {
                        "ServiceName": "Federation Notary",
                        "ServiceTypeIdentifier": "string",
                        "ServiceCurrentStatus": "string",
                        "StatusStartingTime": "string",
                        "ServiceDefinitionURI": "string",
                        "ServiceDigitalIdentity": {
                            "DigitalId": {
                                "X509Certificate": "sgdhfgsfhdsgfhsgfs",
                                "DID": "did:web:essif.iao.fraunhofer.de"
                            }
                        },
                        "AdditionalServiceInformation": {
                            "ServiceBusinessRulesURI": "string",
                            "ServiceGovernanceURI": "string",
                            "ServiceIssuedCredentialTypes": {
                                "CredentialType": [
                                    {
                                        "Type": "string"
                                    },
                                    {
                                        "Type": "string"
                                    }
                                ]
                            },
                            "ServiceContractType": "string",
                            "ServicePolicySet": "string",
                            "ServiceSchemaURI": "string",
                            "ServiceSupplyPoint": "string"
                        }
                    }
                ]
            }
        }
    }
```




# Update TSP
    Url: PATCH-> "http://localhost:16003/tspa-service/tspa/v1/gxfs-prep.testtrain.trust-scheme.de/trust-list/tsp/8271fcbf-0622-4415-b8b1-34ad74215dc6"

    request body: application/json

```
    {
        "TrustServiceProvider": {
            "UUID": "8271fcbf-0622-4415-b8b1-34ad74215dc6",
            "TSPName": "XYZ Gmbh",
            "TSPTradeName": "XYZ Gmbh",
            "TSPInformation": {
                "Address": {
                    "ElectronicAddress": "info@companya.de",
                    "PostalAddress": {
                        "City": "Siegen",
                        "Country": "DE",
                        "PostalCode": "11111",
                        "State": "BW",
                        "StreetAddress1": "Starstr",
                        "StreetAddress2": "071"
                    }
                },
                "TSPCertificationList": {
                    "TSPCertification": [
                        {
                            "Type": "ISO:9001",
                            "Value": "4356546745"
                        },
                        {
                            "Type": "EU-VAT",
                            "Value": "4356546745"
                        }
                    ]
                },
                "TSPEntityIdentifierList": {
                    "TSPEntityIdendifier": [
                        {
                            "Type": "vLEI",
                            "Value": "3453654764"
                        },
                        {
                            "Type": "VAT",
                            "Value": "3453654764"
                        }
                    ]
                },
                "TSPInformationURI": "string"
            },
            "TSPServices": {
                "TSPService": [
                    {
                        "ServiceName": "Federation Notary",
                        "ServiceTypeIdentifier": "string",
                        "ServiceCurrentStatus": "string",
                        "StatusStartingTime": "string",
                        "ServiceDefinitionURI": "string",
                        "ServiceDigitalIdentity": {
                            "DigitalId": {
                                "X509Certificate": "sgdhfgsfhdsgfhsgfs",
                                "DID": "did:web:essif.iao.fraunhofer.de"
                            }
                        },
                        "AdditionalServiceInformation": {
                            "ServiceBusinessRulesURI": "string",
                            "ServiceGovernanceURI": "string",
                            "ServiceIssuedCredentialTypes": {
                                "CredentialType": [
                                    {
                                        "Type": "string"
                                    },
                                    {
                                        "Type": "string"
                                    }
                                ]
                            },
                            "ServiceContractType": "string",
                            "ServicePolicySet": "string",
                            "ServiceSchemaURI": "string",
                            "ServiceSupplyPoint": "string"
                        }
                    }
                ]
            }
        }
    }
```




    
        Note: Here, UUID in url and request body should be same.

# Delete TSP
    Url: DELETE-> "http://localhost:16003/tspa-service/tspa/v1/gxfs-prep.testtrain.trust-scheme.de/trust-list/tsp/8271fcbf-0622-4415-b8b1-34ad74215dc6"



