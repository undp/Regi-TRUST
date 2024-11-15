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
		"serviceIssuedCredentialTypes",
		"serviceGovernanceURI",
		"serviceBusinessRulesURI"
})
public class AdditionalServiceInformationType {
	
	@XmlElement(name = "ServiceIssuedCredentialTypes")
	@JsonProperty("ServiceIssuedCredentialTypes")
	private ServiceIssuedCredentialTypes serviceIssuedCredentialTypes;
	
	@XmlElement(name = "ServiceGovernanceURI")
	@JsonProperty("ServiceGovernanceURI")
	private String serviceGovernanceURI;
	
	@XmlElement(name = "ServiceBusinessRulesURI")
	@JsonProperty("ServiceBusinessRulesURI")
	private String serviceBusinessRulesURI;
	
}
