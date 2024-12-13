module.exports = {
	"TrustServiceProvider": {
		"TSPID": "string",
		"StatusStartingTime": "string",
		"SubmitterInfo": {
			"GivenName": "string",
      "SurName": "string",
			"AgencyName": "string",
      "JobTitle": "string",
			"Address": {
					"ElectronicAddress": "string",
					"PhoneNumber": "string",
					"PostalAddress": {
						"StreetAddress1": "string",
						"StreetAddress2": "string",
						"City": "string",
						"State": "string",
						"Country": "string",
						"PostalCode": "string"
					}
			},
			"Notes": "string"
		}
	},
	"ReviewInfo": {
		"SubmittedDateTime": "string",
		"ReviewStatus": "string",
		"StatusStartingTime": "string",
		"Reviewer": {
			"Username": "string",
			"User_id": "string"
		},
		"Notes": "string"
	}
}
