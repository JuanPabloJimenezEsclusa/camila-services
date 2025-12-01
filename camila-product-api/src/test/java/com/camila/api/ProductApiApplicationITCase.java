package com.camila.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.camila.api.product.infrastructure.adapter.output.mongo.MongoContainerConfig;
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

    assertThat(this.applicationContext).isNotNull()
      .satisfies(ctx -> assertThat(ctx.getBean("productApiApplication")).isNotNull());
  }
}
