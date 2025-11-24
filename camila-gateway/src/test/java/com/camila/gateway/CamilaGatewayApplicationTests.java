package com.camila.gateway;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
@DisplayName("[IT][CamilaGatewayApplication] Spring boot smoke test")
class CamilaGatewayApplicationTests {

  @Autowired
  private ApplicationContext applicationContext;

  @Test
  @DisplayName("[CamilaGatewayApplication] context loaded")
  void contextLoads() {
    Assertions.assertNotNull(applicationContext);
  }

  @Test
  @DisplayName("[CamilaGatewayApplication] main method starts application")
  void mainMethodStartsApplication() {
    CamilaGatewayApplication.main(new String[]{});
    Assertions.assertTrue(true);
  }
}
