package com.camila.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * The type Camila gateway application.
 */
@SpringBootApplication(exclude = ReactiveUserDetailsServiceAutoConfiguration.class)
@EnableDiscoveryClient
public class CamilaGatewayApplication {

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(CamilaGatewayApplication.class, args);
  }
}
