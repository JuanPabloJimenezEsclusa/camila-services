package com.camila.gateway.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * The type Fail back controller.
 */
@RestController
public class FailBackController {

  /**
   * Fallback response entity.
   *
   * @return the response entity
   */
  @GetMapping("/fallback")
  public ResponseEntity<Mono<String>> fallback() {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Mono.just("Service no available - gateway (circuit breaker | retry)"));
  }
}
