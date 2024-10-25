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
@XmlType(name = "NameType",propOrder = {"name"})
public class NameType implements Serializable {

	@XmlElement(name = "Name")
	private String name;

	
	
}