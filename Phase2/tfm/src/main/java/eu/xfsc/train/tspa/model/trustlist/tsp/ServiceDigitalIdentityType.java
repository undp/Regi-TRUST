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
@XmlType(name = "ServiceDigitalIdentityType",propOrder = {"digitalIdType"})
public class ServiceDigitalIdentityType {
	
	@XmlElement(name = "DigitalId")
	private DigitalIdType digitalIdType;
	
}
