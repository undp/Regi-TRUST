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
@XmlType(name = "OtherTSLPointerType",propOrder = {
		"uri"
})
public class OtherTSLPointerType implements Serializable{
	
	@XmlElement(name = "URI")
	@JsonProperty("URI")
	private URIType uri;
		
}
