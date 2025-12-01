package com.camila.gateway.infrastructure.adapter.input.rest;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * The type Fallback controller.
 */
@RestController
class FallbackAdapter {

  private static final Logger log = LoggerFactory.getLogger(FallbackAdapter.class);

  private final String fallbackMessage;

  private final Counter fallbackCounter;

  /**
   * Instantiates a new Fallback adapter.
   *
   * @param meterRegistry   the meter registry
   * @param fallbackMessage the fallback message
   */
  FallbackAdapter(final MeterRegistry meterRegistry,
                  @Value("${gateway.fallback.message:Circuit-breaker-fallback}") final String fallbackMessage) {
    this.fallbackCounter = meterRegistry.counter("gateway.fallback.invocations", "endpoint", "/fallback");
    this.fallbackMessage = fallbackMessage;
  }

  /**
   * Fallback response entity.
   *
   * @param request the request
   * @return the response entity
   */
  @GetMapping("/fallback")
  ResponseEntity<Mono<FallbackResponse>> fallback(final ServerHttpRequest request) {
    try {
      this.fallbackCounter.increment();
    } catch (final Exception e) {
      log.warn("Failed to increment fallback metric", e);
    }

    if (log.isWarnEnabled()) {
      log.warn("Fallback invoked: method={} path={} remote={} headers={}", request.getMethod(), request.getPath(),
        request.getRemoteAddress(), request.getHeaders().toSingleValueMap());
    }

    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
      .body(Mono.just(new FallbackResponse("Service Unavailable", this.fallbackMessage)));
  }

  /**
   * The type Fallback response.
   */
  record FallbackResponse(String message, String details) {
  }
}
