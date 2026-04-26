package com.camila.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * The type Camila gateway application.
 */
@SpringBootApplication(exclude = ReactiveUserDetailsServiceAutoConfiguration.class)
@EnableDiscoveryClient
@SuppressWarnings("PMD.ClassWithOnlyPrivateConstructorsShouldBeFinal") // Because Spring requires a non-final class for
class CamilaGatewayApplication {

  private CamilaGatewayApplication() {
  }

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  static void main(String[] args) {
    SpringApplication.run(CamilaGatewayApplication.class, args);
  }
}
