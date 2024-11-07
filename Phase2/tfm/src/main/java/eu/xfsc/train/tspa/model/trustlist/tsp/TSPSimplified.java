package eu.xfsc.train.tspa.model.trustlist.tsp;

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
@XmlType(name = "TSPSimplified")
public class TSPSimplified {
    @XmlElement(name = "TSPID")
    @JsonProperty("TSPID")
    private String tspID;
    
    @XmlElement(name = "LastUpdate")
    @JsonProperty("LastUpdate")
    private String lastUpdate;
    
    @XmlElement(name = "TSPVersion")
    @JsonProperty("TSPVersion")
    private String tspVersion;
}