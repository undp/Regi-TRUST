package eu.xfsc.train.tspa.model.trustlist;

import java.util.List;

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
		"tslVersion",
		"tslType",
		"listIssueDateTime",
		"nextUpdate",	
		"frameworkName",
		"frameworkOperatorName",
		"frameworkOperatorAddress",
		"frameworkInformationURI",
		"frameworkTypeCommunityRules",
		"schemeTerritory",
		"policyOrLegalNotice",
		"distributionPoints",
		"schemeExtensions",
		"pointersToOtherTSL"
})
public class FrameworkInformationType{
	
	@XmlElement(name = "TSLVersion")
	@JsonProperty("TSLVersion")
	private String tslVersion;
	
	@XmlElement(name = "TSLType")
	@JsonProperty("TSLType")
	private String tslType;

	@XmlElement(name = "ListIssueDateTime")
	@JsonProperty("ListIssueDateTime")
	private String listIssueDateTime;

	@XmlElement(name = "NextUpdate")
	@JsonProperty("NextUpdate")
	private String nextUpdate;
	
	@XmlElement(name = "FrameworkName")
	@JsonProperty("FrameworkName")
	private NameType frameworkName;
	
	@XmlElement(name = "FrameworkOperatorName")
	@JsonProperty("FrameworkOperatorName")
	private NameType frameworkOperatorName;
	
	@XmlElement(name = "FrameworkOperatorAddress")
	@JsonProperty("FrameworkOperatorAddress")
	private FrameworkOperatorAddressType frameworkOperatorAddress;
	
	@XmlElement(name = "FrameworkInformationURI")
	@JsonProperty("FrameworkInformationURI")
	private URIType frameworkInformationURI;
	
	@XmlElement(name = "FrameworkTypeCommunityRules")
	@JsonProperty("FrameworkTypeCommunityRules")
	private URIType frameworkTypeCommunityRules;
	
	@XmlElement(name = "SchemeTerritory")
	@JsonProperty("SchemeTerritory")
	private String schemeTerritory;
	
	@XmlElement(name = "PolicyOrLegalNotice")
	@JsonProperty("PolicyOrLegalNotice")
	private PolicyOrLegalNoticeType policyOrLegalNotice;

	@XmlElement(name = "DistributionPoints")
	@JsonProperty("DistributionPoints")
	private URIType distributionPoints;
	
	@XmlElement(name = "SchemeExtensions")
	@JsonProperty("SchemeExtensions")
	private URIType schemeExtensions;
	
	@XmlElement(name = "PointersToOtherTSL")
	@JsonProperty("PointersToOtherTSL")
	private List<OtherTSLPointerType> pointersToOtherTSL;
	


}
