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
@XmlType(name = "TSPQualifierType",propOrder = {"name", "value", "qualifierURI"})
public class TSPQualifierType {
	
	@XmlElement(name = "Name")
	@JsonProperty("Name")
	private String name; 

	@XmlElement(name = "Value")
	@JsonProperty("Value")
	private String value; 

	@XmlElement(name = "QualifierURI")
	@JsonProperty("QualifierURI")
	private String qualifierURI; 

}
