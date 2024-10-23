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
@XmlType(name = "DigitalIdType",propOrder = {"x509Certificate","did"})
public class DigitalIdType {
	
	@XmlElement(name = "X509Certificate")
	private String x509Certificate;
	
	@XmlElement(name = "DID")
	private String did;

}
