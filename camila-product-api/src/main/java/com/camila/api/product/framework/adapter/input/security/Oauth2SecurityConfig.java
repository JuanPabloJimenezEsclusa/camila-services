package com.camila.api.product.framework.adapter.input.security;

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

/**
 * The type Oauth 2 security config.
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Profile("dev|int")
class Oauth2SecurityConfig {
  private static final String[] PERMITTED = { "/", "/v3/api-docs/**", "/swagger*/**", "/swagger-ui/**", "/webjars/**", "/actuator/**", "/graphiql/**" };
  private static final String[] PRODUCT_ENDPOINTS = { "/products", "/products/**", "/graphql/**", "/ws/**", "/rsocket/**" };

  @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
  private String trustedIssuers;

  /**
   * Security web filter chain.
   *
   * @param http the http
   * @return the security web filter chain
   */
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
