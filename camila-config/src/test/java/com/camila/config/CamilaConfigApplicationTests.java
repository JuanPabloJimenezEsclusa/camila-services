package com.camila.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("[IT][CamilaConfigApplication] Spring boot smoke test")
class CamilaConfigApplicationTests {

  @Test
  @DisplayName("[CamilaConfigApplication] context loaded")
  void contextLoads() {
    Assertions.assertTrue(true);
  }
}
