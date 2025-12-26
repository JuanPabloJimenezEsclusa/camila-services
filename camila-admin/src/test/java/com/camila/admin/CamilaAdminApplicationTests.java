package com.camila.admin;

import static org.assertj.core.api.Assertions.assertThat;

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
    assertThat(this.applicationContext)
      .isNotNull()
      .satisfies(ctx -> assertThat(ctx.containsBean("camilaAdminApplication")).isTrue());
  }
}
