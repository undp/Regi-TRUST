package eu.xfsc.train.tspa.configuration;

import java.util.List;

import javax.management.loading.PrivateClassLoader;

import org.apache.commons.logging.Log;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import eu.xfsc.train.tspa.services.PublicationServiceImpl;

/**
 *  Load the did methods from "application.yml"
 */
@Configuration
@ConfigurationProperties(prefix = "resolverdriver")
public class DIDMethodConfig {
	
	
	private List<String> didmethods;

	public List<String> getDidMethods() {
		return didmethods;
	}

	public void setDidMethods(List<String> methods) {
		this.didmethods = methods;
	}
	
}
