package com.camila.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
@DisplayName("[IT][CamilaConfigApplication] Spring boot smoke test")
class CamilaConfigApplicationTests {

  @Autowired
  private ApplicationContext applicationContext;

  @Test
  @DisplayName("[CamilaConfigApplication] context loaded")
  void contextLoads() {
    Assertions.assertNotNull(applicationContext);
  }

  @Test
  @DisplayName("[CamilaConfigApplication] main method starts application")
  void mainMethodStartsApplication() {
    CamilaConfigApplication.main(new String[]{});
    Assertions.assertTrue(true);
  }
}
