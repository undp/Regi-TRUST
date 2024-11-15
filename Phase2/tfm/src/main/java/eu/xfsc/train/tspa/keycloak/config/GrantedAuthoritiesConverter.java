package eu.xfsc.train.tspa.keycloak.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * By default, authorization is made through scope claims from the JWT Access Token.
 * In order to authorize users by roles, we override the default configuration by implementing
 * a custom converter.
 */
public class GrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        Map<String, Object> realmAccess = source.getClaimAsMap("realm_access");

        if (Objects.nonNull(realmAccess)) {
            List<String> roles = (List<String>) realmAccess.get("roles");

            if (Objects.nonNull(roles)) {
                return roles.stream()
                        .map(rn -> new SimpleGrantedAuthority("ROLE_" + rn))
                        .collect(Collectors.toList());
            }

        }

        return List.of();
    }

}