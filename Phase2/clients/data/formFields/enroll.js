'use strict';
var selectLists = require('../selectLists');

const addressFormComponent = (Addressee) => {
    return {
        "Type": "Wrapper",
        "Key": "Address",
        "Components": [
            {
                "Type": "InlineFields",
                "SubType": "NoHover",
                "Label": "",
                "Key": "",
                "Components": [
                    {
                        "Type": "Text",
                        "SubType": "Email",
                        "Key": "ElectronicAddress",
                        "Label": "Contact Electronic Address",
                        "Validation": {
                            "Required": true
                        }
                    },
                    {
                        "Type": "Text",
                        "SubType": "Tel",
                        "Key": "PhoneNumber",
                        "Label": "Contact Phone Number"
                    }
                ]
            },
            {
                "Type": "Wrapper",
                "Key": "PostalAddress",
                "Components": [
                    {
                        "Type": "Text",
                        "SubType": "Text",
                        "Key": "StreetAddress1",
                        "Label": "Address Line 1"
                    },
                    {
                        "Type": "Text",
                        "SubType": "Text",
                        "Key": "StreetAddress2",
                        "Label": "Address Line 2"
                    },
                    {
                        "Type": "InlineFields",
                        "SubType": "NoHover",
                        "Label": "",
                        "Key": "",
                        "Components": [
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
                            }
                        ]
                    },
                    {
                        "Type": "InlineFields",
                        "SubType": "NoHover",
                        "Label": "",
                        "Key": "",
                        "Components": [
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
        ]
    }
}

const tsp = "TrustServiceProvider"
const tspSubmitter = tsp + "[SubmitterInfo]"

module.exports = {
    "FormSections": [
        {
            "SectionName": "Provider Information",
            "Fields": [
                {
                    "Type": "Wrapper",
                    "Key": tspSubmitter,
                    "Components": [
                        {
                            "Type": "InlineFields",
                            "SubType": "NoHover",
                            "Label": "",
                            "Key": "",
                            "Components": [
                                {
                                    "Type": "Text",
                                    "SubType": "Text",
                                    "Key": "GivenName",
                                    "Label": "Given Name",
                                    "Validation": {
                                        "Required": true
                                    }
                                },
                                {
                                    "Type": "Text",
                                    "SubType": "Text",
                                    "Key": "SurName",
                                    "Label": "Surname",
                                },
                            ]
                        },   

                        {
                            "Type": "Text",
                            "SubType": "Text",
                            "Key": "AgencyName",
                            "Label": "Agency Name",
                            "Validation": {
                                "Required": true
                            }
                        },
                        {
                            "Type": "Text",
                            "SubType": "Text",
                            "Key": "JobTitle",
                            "Label": "Job Title",
                        },

                        addressFormComponent("Submitter"),

                        {
                            "Type": "TextArea",
                            "Key": "Notes",
                            "Label": "Reasons/Notes",
                            "Tooltip": "Enter any notes that may help in the approval process, or reasons for enrolling."
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
