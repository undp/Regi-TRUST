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
@XmlType(name = "TSPCustomType", propOrder = {
    "tspID",
    "tspCurrentStatus",
    "statusStartingTime",
    "tspInformation",
    "submitterInfo",
    "tspServices"
})
public class TSPCustomType {

    @XmlElement(name = "TSPID")
    @JsonProperty("TSPID")
    private String tspID;
    
    @XmlElement(name = "TSPCurrentStatus")
    @JsonProperty("TSPCurrentStatus")
    private String tspCurrentStatus;
    
    @XmlElement(name = "StatusStartingTime")
    @JsonProperty("StatusStartingTime")
    private String statusStartingTime;
    
    @XmlElement(name = "TSPInformation")
    @JsonProperty("TSPInformation")
    private TSPInformationType tspInformation;
    
    @XmlElement(name = "SubmitterInfo")
    @JsonProperty("SubmitterInfo")
    private SubmitterInfoType submitterInfo;
    
    @XmlElement(name = "TSPServices")
    @JsonProperty("TSPServices")
    private TSPServicesListType tspServices;
}
