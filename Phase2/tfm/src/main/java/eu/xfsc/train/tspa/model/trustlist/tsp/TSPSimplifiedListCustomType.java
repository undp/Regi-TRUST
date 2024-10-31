package eu.xfsc.train.tspa.model.trustlist.tsp;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TSPSimplifiedListCustomType", propOrder = {"trustServiceProvider"})
public class TSPSimplifiedListCustomType {
	
	@XmlElement(name = "TSPSimplified")
	private List<TSPSimplifiedEntry> tspSimplified;
	
	@Getter
	@Setter
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "TSPSimplifiedEntry")
	public static class TSPSimplifiedEntry {
		@XmlElement(name = "TSPID")
		private String tspID;
		
		@XmlElement(name = "StatusStartingTime")
		private String statusStartingTime;
		
		@XmlElement(name = "TSPVersion")
		private String tspVersion;
	}
}

