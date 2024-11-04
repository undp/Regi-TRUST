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
@XmlType(name = "TSPSimplified")
public class TSPSimplified {
    @XmlElement(name = "TSPID")
    private String tspID;
    
    @XmlElement(name = "LastUpdate")
    private String statusStartingTime;
    
    @XmlElement(name = "TSPVersion")
    private String tspVersion;
}