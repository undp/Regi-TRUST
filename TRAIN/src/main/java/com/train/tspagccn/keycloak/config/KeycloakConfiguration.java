package com.train.tspagccn.keycloak.config;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfiguration {

  @Bean
  public KeycloakSpringBootConfigResolver KeycloakConfigResolver() {
    return new KeycloakSpringBootConfigResolver();
  }
}
