package eu.xfsc.train.tspa.model.trustlist;

import java.io.Serializable;
import java.util.List;

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
@XmlType(name = "PostalAddressesType",propOrder = {"postalAddress"})
public class PostalAddressesType implements Serializable{
	@XmlElement(name = "PostalAddress")
	@JsonProperty("PostalAddress")
	private List<Address> postalAddress;

	
	
}
