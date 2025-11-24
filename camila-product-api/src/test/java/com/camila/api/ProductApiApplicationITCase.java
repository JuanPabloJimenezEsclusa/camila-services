package com.camila.api;

import com.camila.api.product.infrastructure.adapter.output.mongo.MongoContainerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
@DisplayName("[IT][ProductApiApplication] Spring boot smoke test")
class ProductApiApplicationITCase extends MongoContainerConfig {

  @Autowired
  private ApplicationContext applicationContext;

  @Test
  @DisplayName("[ProductApiApplication] context loaded")
  void contextLoads() {
    Assertions.assertNotNull(applicationContext);
  }

  @Test
  @DisplayName("[ProductApiApplication] main method starts application")
  void mainMethodStartsApplication() {
    ProductApiApplication.main(new String[]{});
    Assertions.assertTrue(true);
  }
}
