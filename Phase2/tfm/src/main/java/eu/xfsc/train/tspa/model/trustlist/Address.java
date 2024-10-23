package eu.xfsc.train.tspa.model.trustlist;

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
	private  String street;
	
	@XmlElement(name = "Locality")
	private String local;
	
	@XmlElement(name = "PostalCode")
	private String postalCode;

	@XmlElement(name = "CountryName")
	private String country;
	
	
}
