module.exports = {
	"TrustServiceProvider": {
		"UID": "string",
		"TSPCurrentStatus": "string",
		"StatusStartingTime": "string",
		"TSPInformation": {
			"TSPName": "string",
			"TSPLegalName": "string",
			"TSPRole": "string",
			"TrustSchemeName": "string",
			"OtherTSL": "string",
			"TSPInformationURI": "string",
			"TSPLegalBasis": "string",
			"TSPEntityIdentifierList": [
				{
					"TSPEntityIdentifier": {
						"Type": "string",
						"Value": "string"
					}
				}
			],
			"TSPCertificationList": [ 
				{
					"TSPCertification": {
						"Type": "string",
						"Value": "string"
					}
				}
			],
			"TSPKeywords": "string",
			"Address": {
				"ElectronicAddress": "string",
				"PostalAddress": {
					"StreetAddress1": "string",
					"StreetAddress2": "string",
					"City": "string",
					"State": "string",
					"Country": "string",
					"PostalCode": "string"
				}     
			}
		},
		"SubmitterInfo": {
			"Name": "string",
			"Address": {
				"ElectronicAddress": "string",
				"PostalAddress": {
					"StreetAddress1": "string",
					"StreetAddress2": "string",
					"City": "string",
					"State": "string",
					"Country": "string",
					"PostalCode": "string"
				}
			}
		},
		"TSPServices": [
			{
				"TSPService": {
					"ServiceCurrentStatus": "string",
					"StatusStartingTime": "string",
					"ServiceName": "string",
					"ServiceTypeIdentifier": "string",
					"ServiceSupplyPoint": "string",
					"ServiceDefinitionURI": "string",
					"ServiceDigitalIdentity": {
						"Value": "string",
						"KeyType": "string"
					},
					"AdditionalServiceInformation": {
						"ServiceIssuedCredentialTypes": [
							{
								"CredentialType": "string"
							}
						],
						"ServiceGovernanceURI": "string",
						"ServiceBusinessRulesURI": "string"
					},
					"OpsAgent": {
						"Name": "string",
						"Address": {
							"ElectronicAddress": "string",
							"PostalAddress": {
								"StreetAddress1": "string",
								"StreetAddress2": "string",
								"City": "string",
								"State": "string",
								"Country": "string",
								"PostalCode": "string"
							}           
						}
					}
				}
			}
		]
	},
	"Submitter": {
		"Username": "string",
		"User_id": "string"
	},
	"ReviewInfo": {
		"SubmittedDateTime": "string",
		"ReviewStatus": "string",
		"StatusStartingTime": "string",
		"Reviewer": {
			"Username": "string",
			"User_id": "string"
		}
	}
}
