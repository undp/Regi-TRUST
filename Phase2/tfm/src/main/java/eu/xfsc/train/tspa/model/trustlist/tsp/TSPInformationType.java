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
@XmlType(name = "TSPInformationType", propOrder = { "tspAddress", "tSPCertificationList", "tSPEntityIdentifierList",
		"tSPInformationURI" })
public class TSPInformationType {

	@XmlElement(name = "Address")
	private TSPAddessType tspAddress;

	@XmlElement(name = "TSPCertificationList")
	private TSPCertificationListType tSPCertificationList;

	@XmlElement(name = "TSPEntityIdentifierList")
	private TSPEntityIdentifierListType tSPEntityIdentifierList;

	@XmlElement(name = "TSPInformationURI")
	private String tSPInformationURI;

}
