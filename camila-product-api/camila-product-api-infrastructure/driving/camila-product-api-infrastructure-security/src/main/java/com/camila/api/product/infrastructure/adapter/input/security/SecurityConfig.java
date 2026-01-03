package com.camila.api.product.infrastructure.adapter.input.security;

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
 * The type Oauth2 security config.
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

  private static final String[] PERMITTED = {
    "/", "/v3/api-docs/**", "/swagger*/**", "/swagger-ui/**", "/webjars/**",
    "/actuator/**",
    "/graphiql/**",
    "/rsocket/**",
    "/ProductService/**"};

  private static final String[] PRODUCT_ENDPOINTS = {"/products", "/products/**", "/graphql/**", "/ws/**"};

  @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:#{null}}")
  private String trustedIssuers;

  /**
   * Basic security web filter chain security web filter chain.
   *
   * @param http the http
   * @return the security web filter chain
   */
  @Bean
  @Profile("default|loc|local-compose|int")
  SecurityWebFilterChain basicSecurityWebFilterChain(final ServerHttpSecurity http) {
    return http
      .cors(ServerHttpSecurity.CorsSpec::disable)
      .csrf(ServerHttpSecurity.CsrfSpec::disable)
      .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
      .build();
  }

  /**
   * Oauth2 security web filter chain security web filter chain.
   *
   * @param http the http
   * @return the security web filter chain
   */
  @Bean
  @Profile("dev|pre|pro")
  SecurityWebFilterChain oauth2SecurityWebFilterChain(final ServerHttpSecurity http) {
    final var authnResolver = JwtIssuerReactiveAuthenticationManagerResolver.fromTrustedIssuers(this.trustedIssuers);
    return http
      .cors(ServerHttpSecurity.CorsSpec::disable)
      .csrf(ServerHttpSecurity.CsrfSpec::disable)
      // delegate the AUTHN to SSO (keycloak, cognito)
      .oauth2ResourceServer(resourceServer -> resourceServer.authenticationManagerResolver(authnResolver))
      // check the AUTHZ
      .authorizeExchange(exchanges -> exchanges.pathMatchers(PERMITTED).permitAll()
        .pathMatchers(HttpMethod.GET, PRODUCT_ENDPOINTS).hasAuthority(Authority.READ.getScope())
        .pathMatchers(HttpMethod.POST, PRODUCT_ENDPOINTS).hasAuthority(Authority.WRITE.getScope())
        .pathMatchers(HttpMethod.PUT, PRODUCT_ENDPOINTS).hasAuthority(Authority.WRITE.getScope())
        .pathMatchers(HttpMethod.DELETE, PRODUCT_ENDPOINTS).hasAuthority(Authority.WRITE.getScope())
        .anyExchange().denyAll())
      .build();
  }

  private enum Authority {
    READ("SCOPE_camila/read"), WRITE("SCOPE_camila/write");
    private final String scope;

    Authority(String scope) {
      this.scope = scope;
    }

    String getScope() {
      return this.scope;
    }
  }
}
