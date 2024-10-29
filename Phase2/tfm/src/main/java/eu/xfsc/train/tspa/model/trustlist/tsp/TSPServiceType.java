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
@XmlType(name = "TSPServiceType",propOrder = {
		"serviceName",
		"serviceTypeIdentifier",
		"serviceCurrentStatus",
		"statusStartingTime",
		"serviceDefinitionURI",
		"serviceDigitalIdentity",
		"additionalServiceInformation"
})
public class TSPServiceType {
	
	@XmlElement(name = "ServiceName")
	@JsonProperty("ServiceName")
	private String serviceName;

	@XmlElement(name = "ServiceTypeIdentifier")
	@JsonProperty("ServiceTypeIdentifier")
	private String serviceTypeIdentifier;
	
	@XmlElement(name = "ServiceCurrentStatus")
	@JsonProperty("ServiceCurrentStatus")
	private String serviceCurrentStatus;
	
	@XmlElement(name = "StatusStartingTime")
	@JsonProperty("StatusStartingTime")
	private String statusStartingTime;
	
	@XmlElement(name = "ServiceDefinitionURI")
	@JsonProperty("ServiceDefinitionURI")
	private String serviceDefinitionURI;
	
	@XmlElement(name = "ServiceDigitalIdentity")
	@JsonProperty("ServiceDigitalIdentity")
	private ServiceDigitalIdentityType serviceDigitalIdentity;
	
	@XmlElement(name = "AdditionalServiceInformation")
	@JsonProperty("AdditionalServiceInformation")
	private AdditionalServiceInformationType additionalServiceInformation;
}
