package com.camila.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("[IT][ProductApiApplication] Spring boot smoke test")
class ProductApiApplicationTests {

  @Test
  @DisplayName("[ProductApiApplication] context loaded")
	void contextLoads() {
    Assertions.assertTrue(true);
	}
}
