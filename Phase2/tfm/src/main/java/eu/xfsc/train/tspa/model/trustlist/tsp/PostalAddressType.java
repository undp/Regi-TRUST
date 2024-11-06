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
@XmlType(name = "PostalAddressType",propOrder = {"locality", "countryName", "postalCode", "state", "streetAddress1","streetAddress2"})
public class PostalAddressType {
	
	@XmlElement(name = "Locality")
	@JsonProperty("Locality")
	private String locality;
	
	@XmlElement(name = "CountryName")
	@JsonProperty("CountryName")
	private String countryName;
	
	@XmlElement(name = "PostalCode")
	@JsonProperty("PostalCode")
	private String postalCode;
	
	@XmlElement(name = "State")
	@JsonProperty("State")
	private String state;
	
	@XmlElement(name = "StreetAddress1")
	@JsonProperty("StreetAddress1")
	private String streetAddress1;
	
	@XmlElement(name = "StreetAddress2")
	@JsonProperty("StreetAddress2")
	private String streetAddress2;

}
