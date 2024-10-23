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
@XmlType(name = "TSPCustomType",propOrder = {
		"uUID",
		"tSPName",
		"tSPTradeName",
		"tSPInformation",
		"tSPServices"
})
public class TSPCustomType {

	@XmlElement(name = "UUID")
	private String uUID;
	
	@XmlElement(name = "TSPName")
	private  String tSPName;
	
	@XmlElement(name = "TSPTradeName")
	private String tSPTradeName;
	
	@XmlElement(name = "TSPInformation")
	private TSPInformationType tSPInformation;
	
	@XmlElement(name = "TSPServices")
	private TSPServicesListType tSPServices;
}
