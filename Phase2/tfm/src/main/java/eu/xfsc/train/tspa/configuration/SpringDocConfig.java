package eu.xfsc.train.tspa.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@OpenAPIDefinition
@Configuration
public class SpringDocConfig {

	 @Bean
	  public OpenAPI baseOpenApi() {
	   

	    return new OpenAPI().info(new Info().title("XFSC-TRAIN").version("1.0.0")
	    		.description("This is the REST API of the XFSC TRAIN TSPA."));
	    }

}
