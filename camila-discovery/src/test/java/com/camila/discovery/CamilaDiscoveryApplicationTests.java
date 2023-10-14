package com.camila.discovery;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("[IT][CamilaDiscoveryApplication] Spring boot smoke test")
class CamilaDiscoveryApplicationTests {

  @Test
  @DisplayName("[CamilaDiscoveryApplication] context loaded")
  void contextLoads() {
    Assertions.assertTrue(true);
  }
}
