package eu.xfsc.train.tspa.model.trustlist.tsp;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AdditionalServiceInformationType",propOrder = {
		"serviceBusinessRulesURI",
		"serviceGovernanceURI",
		"serviceIssuedCredentialTypes",
		"serviceContractType",
		"servicePolicySet",
		"serviceSchemaURI",
		"serviceSupplyPoint"
})
public class AdditionalServiceInformationType {
	
	@XmlElement(name = "ServiceBusinessRulesURI")
	@JsonProperty("ServiceBusinessRulesURI")
	private String serviceBusinessRulesURI;
	
	@XmlElement(name = "ServiceGovernanceURI")
	@JsonProperty("ServiceGovernanceURI")
	private String serviceGovernanceURI;
	
	@XmlElement(name = "ServiceIssuedCredentialTypes")
	@JsonProperty("ServiceIssuedCredentialTypes")
	private ServiceIssuedCredentialType serviceIssuedCredentialTypes;
	
	@XmlElement(name = "ServiceContractType")
	@JsonProperty("ServiceContractType")
	private String serviceContractType;
	
	@XmlElement(name = "ServicePolicySet")
	@JsonProperty("ServicePolicySet")
	private String servicePolicySet;
	
	@XmlElement(name = "ServiceSchemaURI")
	@JsonProperty("ServiceSchemaURI")
	private String serviceSchemaURI;
	
	@XmlElement(name = "ServiceSupplyPoint")
	@JsonProperty("ServiceSupplyPoint")
	private String serviceSupplyPoint;

}
