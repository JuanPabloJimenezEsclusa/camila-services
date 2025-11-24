package com.camila.admin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
@DisplayName("[IT][CamilaAdminApplication] Spring boot smoke test")
class CamilaAdminApplicationTests {

  @Autowired
  private ApplicationContext applicationContext;

  @Test
  @DisplayName("[CamilaAdminApplication] context loaded")
  void contextLoads() {
    Assertions.assertNotNull(applicationContext);
  }

  @Test
  @DisplayName("[CamilaAdminApplication] main method starts application")
  void mainMethodStartsApplication() {
    CamilaAdminApplication.main(new String[]{});
    Assertions.assertTrue(true);
  }
}
