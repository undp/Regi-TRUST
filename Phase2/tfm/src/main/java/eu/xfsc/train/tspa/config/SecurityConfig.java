package eu.xfsc.train.tspa.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import eu.xfsc.train.tspa.controller.TrustFrameWorkPublishController;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * don't use KeycloakWebSecurityConfigurerAdapter
 * it extends WebSecurityConfigurerAdapter which is deprecated
 */

@Configuration
@EnableWebSecurity
//@EnableMethodSecurity(jsr250Enabled = true) // allows using the @RolesAllowed annotation
@EnableMethodSecurity
public class SecurityConfig {

	  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SecurityConfig.class);


	  @Autowired
	  private RestAuthenticationEntryPoint restAuthenticationEntryPoint;
	
	  @Bean
	  MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
	      return new MvcRequestMatcher.Builder(introspector);
	  }
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) 
		throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(authorize -> 
			authorize
			.requestMatchers(antMatcher(HttpMethod.GET, "/test/**")).permitAll()
			.requestMatchers(antMatcher(HttpMethod.GET, "/actuator/**")).permitAll()
			//.requestMatchers(antMatcher(HttpMethod.PUT, "/notary/api/v1/**")).permitAll()
			.requestMatchers(antMatcher(HttpMethod.PUT, "/tspa/v1/**")).hasAuthority("enrolltf")
			.requestMatchers(antMatcher(HttpMethod.PATCH, "/tspa/v1/**")).hasAuthority("enrolltf")
			.requestMatchers(antMatcher(HttpMethod.OPTIONS, "/**")).permitAll()
			.anyRequest().permitAll())
			.oauth2ResourceServer(oauth2Configurer ->
			  oauth2Configurer.jwt(jwtConfigurer ->
			  jwtConfigurer.jwtAuthenticationConverter(jwt -> { Map<String,
			  Collection<String>> realmAccess = jwt.getClaim("realm_access");
			  log.debug("REALM_ACCESS_CLAIM:" + realmAccess);
			  Collection<String> roles = realmAccess.get("roles");
			  log.debug("Roles:"+ roles);
			  //var grantedAuthorities = roles.stream() .map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
			  var grantedAuthorities = roles.stream() .map(role -> new SimpleGrantedAuthority(role)).toList();
			  log.debug("grantedAuthorities:" + grantedAuthorities);
			  return new JwtAuthenticationToken(jwt, grantedAuthorities); })).authenticationEntryPoint(restAuthenticationEntryPoint));
			  http.sessionManagement( t ->
			  t.sessionCreationPolicy(SessionCreationPolicy.STATELESS) );

		log.debug("reached here");

		return http.build();
	}
	
	@Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }

	
}
