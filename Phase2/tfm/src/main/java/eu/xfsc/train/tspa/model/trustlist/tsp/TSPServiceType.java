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
		"serviceInformation",
		"opsAgentInfo"
})
public class TSPServiceType {
	
	@XmlElement(name = "ServiceInformation")
	@JsonProperty("ServiceInformation")
	private ServiceInformationType serviceInformation;

	@XmlElement(name = "OpsAgentInfo")
	@JsonProperty("OpsAgentInfo")
	private OpsAgentInfoType opsAgentInfo;

}
