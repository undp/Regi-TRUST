package eu.xfsc.train.tspa.model.trustlist.tsp;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter; 
import java.util.List;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TSPEntityIdentifierListType",propOrder = {"tSPEntityIdendifier"})
public class TSPEntityIdentifierListType {
	
	@XmlElement(name = "TSPEntityIdendifier")
	private List<TypeValue> tSPEntityIdendifier;

}
