package com.camila.gateway;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("[IT][CamilaGatewayApplication] Spring boot smoke test")
class CamilaGatewayApplicationTests {

  @Test
  @DisplayName("[CamilaGatewayApplication] context loaded")
  void contextLoads() {
    Assertions.assertTrue(true);
  }
}
