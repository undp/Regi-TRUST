# Publish Trust-list 
# Example

# Initial TrustList with XML
    PUT-> "http://localhost:16003/tspa-service/tspa/v1/init/xml/gxfs-prep.testtrain.trust-scheme.de/trust-list"

    Request body: application/xml
         example: <?xml version="1.0" encoding="UTF-8" ?>
                    <TrustServiceStatusList>
                        <FrameworkInformation>
                            <TSLVersionIdentifier>1</TSLVersionIdentifier>
                            <TSLSequenceNumber>1</TSLSequenceNumber>
                            <TSLType>http://TRAIN/TrstSvc/TrustedList/TSLType/federation1-POC</TSLType>
                            <FrameworkOperatorName>
                                <Name>Federation 1</Name>
                            </FrameworkOperatorName>
                            <FrameworkOperatorAddress>
                                <PostalAddresses>
                                    <PostalAddress>
                                        <StreetAddress>Hauptsrasse</StreetAddress>
                                        <Locality>Stuttgart</Locality>
                                        <PostalCode>70563</PostalCode>
                                        <CountryName>DE</CountryName>
                                    </PostalAddress>
                                </PostalAddresses>
                                <ElectronicAddress>
                                    <URI>mailto:admin@federation1.de</URI>
                                </ElectronicAddress>
                            </FrameworkOperatorAddress>
                            <FrameworkName>
                                <Name>federation1.train.trust-Framework.de</Name>
                            </FrameworkName>
                            <FrameworkInformationURI>
                                <URI>https://TRAIN/interoperability/federation-Directory</URI>
                            </FrameworkInformationURI>
                            <FrameworkAuditURI>
                                <URI>https://TRAIN/interoperability/Audit</URI>
                            </FrameworkAuditURI>
                            <FrameworkTypeCommunityRules>
                                <URI>https://TrustFramework_TRAIN.example.com/en/federation1-dir-rules.html</URI>
                            </FrameworkTypeCommunityRules>
                            <FrameworkScope>EU</FrameworkScope>
                            <PolicyOrLegalNotice>
                                <TSLLegalNotice>The applicable legal framework for the present trusted list is TBD. Valid legal notice text will be created.</TSLLegalNotice>
                            </PolicyOrLegalNotice>
                            <ListIssueDateTime>2023-12-15T00:00:00Z</ListIssueDateTime>
                        </FrameworkInformation>
                    </TrustServiceStatusList>

# Initial TrustList with Json
    PUT-> "http://localhost:16003/tspa-service/tspa/v1/init/json/gxfs-prep.testtrain.trust-scheme.de/trust-list"

    Request body: application/json

   ```
 {
        "TrustServiceStatusList": {
            "FrameworkInformation": {
                "TSLVersionIdentifier": "1",
                "TSLSequenceNumber": "1",
                "TSLType": "http://TRAIN/TrstSvc/TrustedList/TSLType/federation1-POC",
                "FrameworkOperatorName": {
                    "Name": "Federation 1"
                },
                "FrameworkOperatorAddress": {
                    "PostalAddresses": {
                        "PostalAddress": [
                            {
                                "StreetAddress": "Hauptsrasse",
                                "Locality": "Stuttgart",
                                "PostalCode": "70563",
                                "CountryName": "DE"
                            }
                        ]
                    },
                    "ElectronicAddress": {
                        "URI": "mailto:admin@federation1.de"
                    }
                },
                "FrameworkName": {
                    "Name": "federation1.train.trust-Framework.de"
                },
                "FrameworkInformationURI": {
                    "URI": "https://TRAIN/interoperability/federation-Directory"
                },
                "FrameworkAuditURI": {
                    "URI": "https://TRAIN/interoperability/Audit"
                },
                "FrameworkTypeCommunityRules": {
                    "URI": "https://TrustFramework_TRAIN.example.com/en/federation1-dir-rules.html"
                },
                "FrameworkScope": "EU",
                "PolicyOrLegalNotice": {
                    "TSLLegalNotice": "The applicable legal framework for the present trusted list is   TBD. Valid legal notice text will be created."
                },
                "ListIssueDateTime": "2023-12-15T00:00:00Z"
            }
        }
    }
```




# Get Trustlist
    GET-> "http://localhost:16003/tspa-service/tspa/v1/gxfs-prep.testtrain.trust-scheme.de/trust-list"

# Get VC
    GET-> "http://localhost:16003/tspa-service/tspa/v1/gxfs-prep.testtrain.trust-scheme.de/vc/trust-list"

# Delete Trustlist
    DELETE-> "http://localhost:16003/tspa-service/tspa/v1/gxfs-prep.testtrain.trust-scheme.de/trust-list"
