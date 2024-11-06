package eu.xfsc.train.tspa.model.trustlist.tsp;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.xfsc.train.tspa.model.trustlist.NameType;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TSPServiceType", propOrder = {
		"serviceTypeIdentifier",
		"serviceName",
		"serviceDigitalIdentity",
		"serviceStatus",
		"statusStartingTime",
		"schemeServiceDefinition",
		"serviceSupplyPoint",
		"serviceDefinitionURI",
		"additionalServiceInformation"
})
public class ServiceInformationType {
	
	@XmlElement(name = "ServiceTypeIdentifier")
	@JsonProperty("ServiceTypeIdentifier")
	private String serviceTypeIdentifier;

	@XmlElement(name = "ServiceName")
	@JsonProperty("ServiceName")
	private NameType serviceName;

	@XmlElement(name = "ServiceDigitalIdentity")
	@JsonProperty("ServiceDigitalIdentity")
	private ServiceDigitalIdentityType serviceDigitalIdentity;
	
	@XmlElement(name = "ServiceStatus")
	@JsonProperty("ServiceStatus")
	private String serviceStatus;
	
	@XmlElement(name = "StatusStartingTime")
	@JsonProperty("StatusStartingTime")
	private String statusStartingTime;
	
	@XmlElement(name = "SchemeServiceDefinition")
	@JsonProperty("SchemeServiceDefinition")
	private String schemeServiceDefinition;
	
	@XmlElement(name = "ServiceSupplyPoint")
	@JsonProperty("ServiceSupplyPoint")
	private String serviceSupplyPoint;

	@XmlElement(name = "ServiceDefinitionURI")
	@JsonProperty("ServiceDefinitionURI")
	private String serviceDefinitionURI;

	@XmlElement(name = "AdditionalServiceInformation")
	@JsonProperty("AdditionalServiceInformation")
	private AdditionalServiceInformationType additionalServiceInformation;
}
