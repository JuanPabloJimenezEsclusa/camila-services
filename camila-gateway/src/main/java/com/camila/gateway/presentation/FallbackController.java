package com.camila.gateway.presentation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * The type Fallback controller.
 */
@RestController
class FallbackController {

  @Value("${gateway.fallback.message:Circuit-breaker-fallback}")
  private String fallbackMessage;

  /**
   * Fallback response entity.
   *
   * @return the response entity
   */
  @GetMapping("/fallback")
  ResponseEntity<Mono<FallbackResponse>> fallback() {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Mono.just(
      new FallbackResponse("Service Unavailable",  fallbackMessage)));
  }
}

/**
 * The type Fallback response.
 */
record FallbackResponse(String message, String details) {}
