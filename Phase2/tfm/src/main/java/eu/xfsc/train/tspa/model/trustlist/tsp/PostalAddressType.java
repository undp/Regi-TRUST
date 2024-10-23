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
@XmlType(name = "PostalAddressType",propOrder = {"city", "country", "postalCode", "state", "streetAddress1","streetAddress2"})
public class PostalAddressType {
	
	@XmlElement(name = "City")
	private String city;
	
	@XmlElement(name = "Country")
	private String country;
	
	@XmlElement(name = "PostalCode")
	private String postalCode;
	
	@XmlElement(name = "State")
	private String state;
	
	@XmlElement(name = "StreetAddress1")
	private String streetAddress1;
	
	@XmlElement(name = "StreetAddress2")
	private String streetAddress2;

}
