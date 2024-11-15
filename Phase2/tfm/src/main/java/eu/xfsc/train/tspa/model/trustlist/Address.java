package eu.xfsc.train.tspa.model.trustlist;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Address",propOrder = {
		"street",
		"local",
		"postalCode",
		"country"
})
public class Address implements Serializable{
	
	@XmlElement(name = "StreetAddress")
	@JsonProperty("StreetAddress")
	private String street;
	
	@XmlElement(name = "Locality")
	@JsonProperty("Locality")
	private String local;
	
	@XmlElement(name = "PostalCode")
	@JsonProperty("PostalCode")
	private String postalCode;

	@XmlElement(name = "CountryName")
	@JsonProperty("CountryName")
	private String country;
	
	
}
