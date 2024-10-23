package eu.xfsc.train.tspa.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.xfsc.train.tspa.model.trustlist.TrustServiceStatusList;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

@Configuration
public class TSPConfig {
	@Bean
	public JAXBContext setJAXBContext() throws JAXBException {
		return JAXBContext.newInstance(TrustServiceStatusList.class);
	}

}
