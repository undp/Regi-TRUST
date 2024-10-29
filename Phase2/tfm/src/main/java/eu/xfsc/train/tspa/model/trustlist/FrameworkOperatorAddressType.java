package eu.xfsc.train.tspa.model.trustlist;

import java.io.Serializable;

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
@XmlType(name = "FrameworkOperatorAddressType",propOrder = {"postalAddresses","electronicAddress"})
public class FrameworkOperatorAddressType implements Serializable{
	
	@XmlElement(name = "PostalAddresses")
	@JsonProperty("PostalAddresses")
	private PostalAddressesType postalAddresses;
	
	@XmlElement(name = "ElectronicAddress")
	@JsonProperty("ElectronicAddress")
	private URIType electronicAddress;

	
	
}
