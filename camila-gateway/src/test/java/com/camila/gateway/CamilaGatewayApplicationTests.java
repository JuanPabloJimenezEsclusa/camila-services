package com.camila.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(properties = "spring.profiles.active=test")
@DisplayName("[IT][CamilaGatewayApplication] Spring boot smoke test")
class CamilaGatewayApplicationTests {

  @Autowired
  private ApplicationContext applicationContext;

  @Test
  @DisplayName("[CamilaGatewayApplication] context loaded")
  void contextLoads() {
    assertThat(this.applicationContext)
      .isNotNull()
      .satisfies(ctx -> assertThat(ctx.containsBean("camilaGatewayApplication")).isTrue());
  }
}
