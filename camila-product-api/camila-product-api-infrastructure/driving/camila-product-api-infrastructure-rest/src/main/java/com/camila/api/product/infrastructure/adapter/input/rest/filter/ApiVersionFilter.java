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

@Slf4j
@Order(1)
@Component
record ApiVersionFilter(
  @Value("${info.app.version}")
  String apiVersion
) implements WebFilter {

  private static final String API_VERSION_HEADER = "X-Api-Version";

  @Override
  public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
    final var requestApiVersion = Optional
      .ofNullable(exchange.getRequest().getHeaders().getFirst(API_VERSION_HEADER))
      .orElse(this.apiVersion);

    exchange.getResponse().getHeaders().set(API_VERSION_HEADER, requestApiVersion);
    if (!requestApiVersion.equalsIgnoreCase(this.apiVersion)) {
      log.warn("API version mismatch: requested {} but service supports {}", requestApiVersion, this.apiVersion);
      exchange.getResponse().setStatusCode(HttpStatus.NOT_ACCEPTABLE);
      return Mono.empty();
    }

    return chain.filter(exchange);
  }
}
