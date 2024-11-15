package eu.xfsc.train.tspa.model.trustlist.tsp;

import lombok.Getter;
import lombok.Setter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.xfsc.train.tspa.model.trustlist.NameType;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubmitterInfoType", propOrder = {
    "submitterName",
    "submitterAddress"
})
public class SubmitterInfoType {

    @XmlElement(name = "SubmitterName")
    @JsonProperty("SubmitterName")
    private NameType submitterName;

    @XmlElement(name = "SubmitterAddress")
    @JsonProperty("SubmitterAddress")
    private TSPAddessType submitterAddress;
} 