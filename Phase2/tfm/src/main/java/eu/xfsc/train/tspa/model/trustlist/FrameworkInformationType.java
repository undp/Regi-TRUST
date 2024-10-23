package eu.xfsc.train.tspa.model.trustlist;

import java.io.Serializable;

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
		"tSLVersionIdentifier",
		"tSLSequenceNumber",
		"tSLType",
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
	private int tSLVersionIdentifier;
	
	@XmlElement(name = "TSLSequenceNumber")
	private int tSLSequenceNumber;
	
	@XmlElement(name = "TSLType")
	private String tSLType;

	@XmlElement(name = "FrameworkOperatorName")
	private NameType frameworkOperatorName;
	
	@XmlElement(name = "FrameworkOperatorAddress")
	private FrameworkOperatorAddressType frameworkOperatorAddress;
	
	@XmlElement(name = "FrameworkName")
	private  NameType frameworkName;
	
	@XmlElement(name = "FrameworkInformationURI")
	private URIType frameworkInformationURI;
	
	@XmlElement(name = "FrameworkAuditURI")
	private URIType frameworkAuditURI;
	
	@XmlElement(name = "FrameworkTypeCommunityRules")
	private URIType frameworkTypeCommunityRules;
	
	@XmlElement(name = "FrameworkScope")
	private String frameworkScope;
	
	@XmlElement(name = "PolicyOrLegalNotice")
	private PolicyOrLegalNoticeType policyOrLegalNotice;
	
	@XmlElement(name = "ListIssueDateTime")
	private String listIssueDateTime;

	
}
