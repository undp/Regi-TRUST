package eu.xfsc.train.tspa.model.trustlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FrameworkInformationType",propOrder = {
		"tslVersionIdentifier",
		"tslSequenceNumber",
		"tslType",
		"frameworkOperatorName",
		"frameworkOperatorAddress",
		"frameworkName",
		"frameworkInformationURI",
		"frameworkAuditURI",
		"frameworkTypeCommunityRules",
		"frameworkScope",
		"policyOrLegalNotice",
		"listIssueDateTime"	
})
public class FrameworkInformationType{
	
	@XmlElement(name = "TSLVersionIdentifier")
	@JsonProperty("TSLVersionIdentifier")
	private int tslVersionIdentifier;
	
	@XmlElement(name = "TSLSequenceNumber")
	@JsonProperty("TSLSequenceNumber")
	private int tslSequenceNumber;
	
	@XmlElement(name = "TSLType")
	@JsonProperty("TSLType")
	private String tslType;

	@XmlElement(name = "FrameworkOperatorName")
	@JsonProperty("FrameworkOperatorName")
	private NameType frameworkOperatorName;
	
	@XmlElement(name = "FrameworkOperatorAddress")
	@JsonProperty("FrameworkOperatorAddress")
	private FrameworkOperatorAddressType frameworkOperatorAddress;
	
	@XmlElement(name = "FrameworkName")
	@JsonProperty("FrameworkName")
	private NameType frameworkName;
	
	@XmlElement(name = "FrameworkInformationURI")
	@JsonProperty("FrameworkInformationURI")
	private URIType frameworkInformationURI;
	
	@XmlElement(name = "FrameworkAuditURI")
	@JsonProperty("FrameworkAuditURI")
	private URIType frameworkAuditURI;
	
	@XmlElement(name = "FrameworkTypeCommunityRules")
	@JsonProperty("FrameworkTypeCommunityRules")
	private URIType frameworkTypeCommunityRules;
	
	@XmlElement(name = "FrameworkScope")
	@JsonProperty("FrameworkScope")
	private String frameworkScope;
	
	@XmlElement(name = "PolicyOrLegalNotice")
	@JsonProperty("PolicyOrLegalNotice")
	private PolicyOrLegalNoticeType policyOrLegalNotice;
	
	@XmlElement(name = "ListIssueDateTime")
	@JsonProperty("ListIssueDateTime")
	private String listIssueDateTime;

	
}
