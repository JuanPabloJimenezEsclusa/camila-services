package com.camila.api.product.infrastructure.adapter.input.rest.filter;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * ApiVersionFilter is a WebFlux filter that validates the API version provided in the request headers.
 * If the API version in the request does not match the expected version, the filter responds with
 * an HTTP 406 (Not Acceptable) status and stops further processing of the request.
 */
@Slf4j
@Order(1)
@Component
public class ApiVersionFilter implements WebFilter {

  private static final String API_VERSION_HEADER = "X-Api-Version";

  @Value("${info.app.version}")
  private String apiVersion;

  @Override
  public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
    final var requestApiVersion = Optional
      .ofNullable(exchange.getRequest().getHeaders().getFirst(API_VERSION_HEADER))
      .orElse(apiVersion);

    if (!requestApiVersion.equalsIgnoreCase(apiVersion)) {
      log.warn("API version mismatch: requested {} but service supports {}", requestApiVersion, apiVersion);
      exchange.getResponse().setStatusCode(HttpStatus.NOT_ACCEPTABLE);
      return Mono.empty();
    }

    return chain.filter(exchange);
  }
}
