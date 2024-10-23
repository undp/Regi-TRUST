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
	private String serviceName;

	@XmlElement(name = "ServiceTypeIdentifier")
	private String serviceTypeIdentifier;
	
	@XmlElement(name = "ServiceCurrentStatus")
	private String serviceCurrentStatus;
	
	@XmlElement(name = "StatusStartingTime")
	private String statusStartingTime;
	
	@XmlElement(name = "ServiceDefinitionURI")
	private String serviceDefinitionURI;
	
	@XmlElement(name = "ServiceDigitalIdentity")
	private ServiceDigitalIdentityType serviceDigitalIdentity;
	
	@XmlElement(name = "AdditionalServiceInformation")
	private AdditionalServiceInformationType additionalServiceInformation;
}
