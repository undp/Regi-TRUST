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
@XmlType(name = "TSPInformationType", propOrder = {
    "tspName",
    "tspType",
    "trustSchemeName",
    "tspRole",
    "tspLegalName",
    "tspTradeName",
    "tspEntityIdentifierList",
    "tspCertificationLists",
    "tspAddress",
    "tspInformationURI",
    "tspQualifierList",
    "otherTSL"
})
public class TSPInformationType {

    @XmlElement(name = "TSPName")
    @JsonProperty("TSPName")
    private String tspName;

    @XmlElement(name = "TSPType")
    @JsonProperty("TSPType")
    private String tspType;

    @XmlElement(name = "TrustSchemeName")
    @JsonProperty("TrustSchemeName")
    private String trustSchemeName;

    @XmlElement(name = "TSPRole")
    @JsonProperty("TSPRole")
    private String tspRole;

    @XmlElement(name = "TSPLegalName")
    @JsonProperty("TSPLegalName")
    private String tspLegalName;

    @XmlElement(name = "TSPTradeName")
    @JsonProperty("TSPTradeName")
    private String tspTradeName;

    @XmlElement(name = "TSPEntityIdentifierList")
    @JsonProperty("TSPEntityIdentifierList")
    private TSPEntityIdentifierListType tspEntityIdentifierList;

    @XmlElement(name = "TSPCertificationLists")
    @JsonProperty("TSPCertificationLists")
    private String tspCertificationLists;

    @XmlElement(name = "TSPAddress")
    @JsonProperty("TSPAddress")
    private String tspAddress;

    @XmlElement(name = "TSPInformationURI")
    @JsonProperty("TSPInformationURI")
    private String tspInformationURI;

    @XmlElement(name = "TSPQualifierList")
    @JsonProperty("TSPQualifierList")
    private String tspQualifierList;

    @XmlElement(name = "OtherTSL")
    @JsonProperty("OtherTSL")
    private String otherTSL;
}
