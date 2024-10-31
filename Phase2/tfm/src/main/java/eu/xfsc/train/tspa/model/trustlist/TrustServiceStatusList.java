package eu.xfsc.train.tspa.model.trustlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.xfsc.train.tspa.model.trustlist.tsp.TSPSimplifiedListCustomType;
import eu.xfsc.train.tspa.model.trustlist.tsp.TrustServiceProviderListCustomType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "TrustServiceStatusList")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TrustServiceStatusList",propOrder = {"frameworkInformation","trustServiceProviderList"})
@JsonPropertyOrder({"frameworkInformation", "trustServiceProviderList"})
public class TrustServiceStatusList {
	
	@XmlElement(name = "FrameworkInformation")
	@JsonProperty("FrameworkInformation")
	private FrameworkInformationType frameworkInformation;
	
	@XmlElement(name = "TSPSimplifiedList")
	@JsonProperty("TSPSimplifiedList")
	private TSPSimplifiedListCustomType tspSimplifiedList;

}
