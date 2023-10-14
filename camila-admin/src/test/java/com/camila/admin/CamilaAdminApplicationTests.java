package com.camila.admin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("[IT][CamilaAdminApplication] Spring boot smoke test")
class CamilaAdminApplicationTests {

  @Test
  @DisplayName("[CamilaAdminApplication] context loaded")
  void contextLoads() {
    Assertions.assertTrue(true);
  }
}
