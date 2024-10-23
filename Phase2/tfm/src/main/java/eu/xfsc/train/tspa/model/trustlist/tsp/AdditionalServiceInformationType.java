package eu.xfsc.train.tspa.model.trustlist.tsp;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

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
	private String serviceBusinessRulesURI;
	
	@XmlElement(name = "ServiceGovernanceURI")
	private String serviceGovernanceURI;
	
	@XmlElement(name = "ServiceIssuedCredentialTypes")
	private ServiceIssuedCredentialType serviceIssuedCredentialTypes;
	
	@XmlElement(name = "ServiceContractType")
	private String serviceContractType;
	
	@XmlElement(name = "ServicePolicySet")
	private String servicePolicySet;
	
	@XmlElement(name = "ServiceSchemaURI")
	private String serviceSchemaURI;
	
	@XmlElement(name = "ServiceSupplyPoint")
	private String serviceSupplyPoint;

}
