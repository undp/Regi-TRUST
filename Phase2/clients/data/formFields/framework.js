'use strict';
var selectLists = require('../selectLists');

const framework = "FrameworkInformation"

const addressFormComponent = (Addressee) => {
    return {
        "Type": "Wrapper",
        "Key": Addressee + "Address",
        "Components": [
            {
                "Type": "Wrapper",
                "Key": "PostalAddresses",
                "Components": [
                    {
                        "Type": "Wrapper",
                        "Key": "PostalAddress",
                        "Components": [
                            {
                                "Type": "Wrapper",
                                "Key": "0",
                                "Components": [
                                    {
                                        "Type": "Text",
                                        "SubType": "Text",
                                        "Key": "StreetAddress",
                                        "Label": "Street Address"
                                    },
                                    {
                                        "Type": "Text",
                                        "SubType": "Text",
                                        "Key": "Locality",
                                        "Label": "Locality"
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
                                        "Key": "CountryName",
                                        "Label": "Country",
                                        "Values": selectLists.CountryList.map(country => ({"Value": country, "Label": country}))    //TODO: the country id from backend needs to match values in selectList.js
                                    }
                                ]
                            }
                        ]
                    }
                ]
            },
            {
                "Type": "Wrapper",
                "Key": "ElectronicAddress",
                "Components": [
                    {
                        "Type": "Text",
                        "SubType": "Text",
                        "Key": "URI",
                        "Label": "Electronic Address"
                    }
                ]
            }
        ]
    }
}

module.exports = {
    "FormSections": [
        {
            "SectionName": "Framework Information",
            "Fields": [
                {
                    "Type": "Wrapper",
                    "Key": framework,
                    "Components": [
                        {
                            "Type": "Hidden",
                            "Key": "TSLVersion"
                        },
                        {
                            "Type": "Hidden",
                            "Key": "ListIssueDateTime"
                        },
                        {
                            "Type": "Hidden",
                            "Key": "NextUpdate"
                        },
                        {
                            "Type": "Wrapper",
                            "Key": "FrameworkName",
                            "Components": [
                                {
                                    "Type": "Text",
                                    "SubType": "Text",
                                    "Key": "Name",
                                    "Label": "Name",
                                    "Validation": {
                                        "Required": true
                                    },
                                    "Attributes": {
                                        "disabled": true
                                    }
                                }
                            ]
                        },
                        {
                            "Type": "Wrapper",
                            "Key": "FrameworkOperatorName",
                            "Components": [
                                {
                                    "Type": "Text",
                                    "SubType": "Text",
                                    "Key": "Name",
                                    "Label": "Operator Name",
                                    "Validation": {
                                        "Required": true
                                    }
                                }
                            ]
                        },
                        {
                            "Type": "Text",
                            "SubType": "URL",
                            "Key": "TSLType",
                            "Label": "Trust List Type (URL)"
                        },
                        addressFormComponent("FrameworkOperator"),
                        {
                            "Type": "Wrapper",
                            "Key": "FrameworkInformationURI",
                            "Components": [
                                {
                                    "Type": "Text",
                                    "SubType": "URL",
                                    "Key": "URI",
                                    "Label": "Framework Information URI",
                                    "Validation": {
                                        "Required": true
                                    }
                                }
                            ]
                        },
                        {
                            "Type": "Wrapper",
                            "Key": "FrameworkTypeCommunityRules",
                            "Components": [
                                {
                                    "Type": "Text",
                                    "SubType": "URL",
                                    "Key": "URI",
                                    "Label": "Framework Community Rules URI"
                                }
                            ]
                        },
                        {
                            "Type": "Text",
                            "SubType": "Text",
                            "Key": "SchemeTerritory",
                            "Label": "Scheme Territory"
                        },
                        {
                            "Type": "Wrapper",
                            "Key": "PolicyOrLegalNotice",
                            "Components": [
                                {
                                    "Type": "Text",
                                    "SubType": "Text",
                                    "Key": "TSLLegalNotice",
                                    "Label": "Policy or Legal Notices"
                                }
                            ]
                        },
                        {
                            "Type": "Wrapper",
                            "Key": "DistributionPoints",
                            "Components": [
                                {
                                    "Type": "Text",
                                    "SubType": "URL",
                                    "Key": "URI",
                                    "Label": "Distribution URL"
                                }
                            ]
                        },
                        {
                            "Type": "Wrapper",
                            "Key": "SchemeExtensions",
                            "Components": [
                                {
                                    "Type": "Text",
                                    "SubType": "URL",
                                    "Key": "URI",
                                    "Label": "Extensions"
                                }
                            ]
                        },
                        {
                            "Type": "Wrapper",
                            "Key": "PointersToOtherTSL",
                            "Components": [
                                {
                                    "Type": "Wrapper",
                                    "Key": "0",
                                    "Components": [
                                        {
                                            "Type": "Text",
                                            "SubType": "URL",
                                            "Key": "URI",
                                            "Label": "Pointer to Other Trust Lists"
                                        }
                                    ]
                                }
                            ]
                        },
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
