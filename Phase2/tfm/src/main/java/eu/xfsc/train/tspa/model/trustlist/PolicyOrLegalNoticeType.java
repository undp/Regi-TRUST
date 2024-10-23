package eu.xfsc.train.tspa.model.trustlist;

import java.io.Serializable;
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
@XmlType(name = "PolicyOrLegalNoticeType",propOrder = {"tSLLegalNotice"})
public class PolicyOrLegalNoticeType implements Serializable{
	
	@XmlElement(name="TSLLegalNotice")
	private String tSLLegalNotice;

}
