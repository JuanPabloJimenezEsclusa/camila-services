package com.camila.api.product.framework.adapter.input.rest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerReactiveAuthenticationManagerResolver;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Profile("!default&!loc")
class SecurityConfig {
  private static final String[] PERMITTED = { "/", "/v3/api-docs/**", "/swagger*/**", "/swagger-ui/**", "/webjars/**", "/actuator/**" };
  private static final String[] PRODUCT_ENDPOINTS = { "/products", "/products/**" };

  @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
  private String trustedIssuers;

  @Bean
  SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    var authenticationManagerResolver = JwtIssuerReactiveAuthenticationManagerResolver.fromTrustedIssuers(trustedIssuers);
    return http
      // delegamos la autenticación al servicio SSO (keycloak)
      .oauth2ResourceServer(resourceServer -> resourceServer.authenticationManagerResolver(authenticationManagerResolver))
      // comprobamos la autorización
      .authorizeExchange(exchanges -> exchanges
        .pathMatchers(PERMITTED).permitAll()
        .pathMatchers(HttpMethod.GET, PRODUCT_ENDPOINTS).hasAuthority(Authority.READ.getScope())
        .pathMatchers(HttpMethod.POST, PRODUCT_ENDPOINTS).hasAuthority(Authority.WRITE.getScope())
        .pathMatchers(HttpMethod.PUT, PRODUCT_ENDPOINTS).hasAuthority(Authority.WRITE.getScope())
        .pathMatchers(HttpMethod.DELETE, PRODUCT_ENDPOINTS).hasAuthority(Authority.WRITE.getScope())
        .anyExchange().denyAll())
      .cors(ServerHttpSecurity.CorsSpec::disable)
      .csrf(ServerHttpSecurity.CsrfSpec::disable)
      .build();
  }

  private enum Authority {
    READ("SCOPE_camila.read"),
    WRITE("SCOPE_camila.write");
    private final String scope;

    Authority(String scope) {
      this.scope = scope;
    }

    public String getScope() {
      return this.scope;
    }
  }
}
