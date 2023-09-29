package com.camila.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The type Product API application tests.
 */
@SpringBootTest
@DisplayName("[IT][ProductApiApplication] Spring boot smoke test")
class ProductApiApplicationTests {
  /**
   * Context loads.
   */
  @Test
  @DisplayName("[ProductApiApplication] context loaded")
	void contextLoads() {
    Assertions.assertTrue(true);
	}
}
