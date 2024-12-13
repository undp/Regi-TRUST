const mongoose = require('mongoose');

// Define the nested schemas
const PostalAddressSchema = new mongoose.Schema({
  StreetAddress1: String,
  StreetAddress2: String,
  Locality: String,
  State: String,
  CountryName: String,
  PostalCode: String,
});

const ElectronicAddressSchema = new mongoose.Schema({
  URI: String,
});

const AddressSchema = new mongoose.Schema({
  ElectronicAddress: ElectronicAddressSchema,
  PostalAddresses: [PostalAddressSchema],
});

const EntityIdentifierSchema = new mongoose.Schema({
  Type: String,
  Value: String,
});

const CertificationSchema = new mongoose.Schema({
  Type: String,
  Value: String,
});

const DigitalIdSchema = new mongoose.Schema({
  Value: String,
  KeyType: String,
});

const ServiceDigitalIdentitySchema = new mongoose.Schema({
  DigitalId: DigitalIdSchema,
});

const AdditionalServiceInfoSchema = new mongoose.Schema({
  ServiceIssuedCredentialTypes: {
    CredentialType: String,
  },
  ServiceGovernanceURI: String,
  ServiceBusinessRulesURI: String,
});

const ServiceInformationSchema = new mongoose.Schema({
  ServiceStatus: String,
  StatusStartingTime: String,
  ServiceName: {
    Name: String,
  },
  ServiceTypeIdentifier: String,
  ServiceSupplyPoint: String,
  ServiceDefinitionURI: String,
  ServiceDigitalIdentity: ServiceDigitalIdentitySchema,
  AdditionalServiceInformation: AdditionalServiceInfoSchema,
});

const OpsAgentAddressSchema = new mongoose.Schema({
  ElectronicAddress: {
    URI: String
  },
  PostalAddresses: [PostalAddressSchema],
});

const OpsAgentInfoSchema = new mongoose.Schema({
  OpsAgentName: {
    Name: String,
  },
  OpsAgentAddress: OpsAgentAddressSchema,
});

const TSPServiceSchema = new mongoose.Schema({
  ServiceInformation: ServiceInformationSchema,
  OpsAgentInfo: OpsAgentInfoSchema,
});

const ReviewerSchema = new mongoose.Schema({
  Username: String,
  User_id: String,
});

const ReviewInfoSchema = new mongoose.Schema({
  SubmittedDateTime: String,
  ReviewStatus: String,
  StatusStartingTime: String,
  Reviewer: ReviewerSchema,
});

const SubmitterInfoSchema = new mongoose.Schema({
  SubmitterName: {
    Name: String,
  },
  SubmitterAddress: AddressSchema,
});

const TSPInformationSchema = new mongoose.Schema({
  TSPName: {
    Name: String,
  },
  TSPLegalName: {
    Name: String,
  },
  TSPRole: String,
  TrustSchemeName: String,
  OtherTSL: String,
  TSPInformationURI: {
    URI: String,
  },
  TSPLegalBasis: String,
  TSPEntityIdentifierList: {
    TSPEntityIdentifier: [EntityIdentifierSchema],
  },
  TSPCertificationLists: {
    TSPCertification: [CertificationSchema],
  },
  TSPKeywords: String,
  TSPAddress: AddressSchema,
});

const SubmitterSchema = new mongoose.Schema({
  Username: String,
  User_id: String,
});

const TSPSchema = new mongoose.Schema({
	TrustServiceProvider: {
		TSPID: String,
		TSPCurrentStatus: String,
		StatusStartingTime: String,
		TSPInformation: TSPInformationSchema,
		SubmitterInfo: SubmitterInfoSchema,
		TSPServices: {
			TSPService: [TSPServiceSchema],
		},
	},
  Submitter: SubmitterSchema,
  ReviewInfo: ReviewInfoSchema,
});

module.exports = mongoose.model('Submission', TSPSchema, 'Submissions');
