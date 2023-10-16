package com.camila.gateway.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
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
  @RequestMapping("/fallback")
  public ResponseEntity<Mono<String>> fallback() {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Mono.just("Service no available"));
  }
}
