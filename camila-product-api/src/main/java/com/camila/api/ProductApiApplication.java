package com.camila.api;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import reactor.core.publisher.Hooks;

/**
 * Main class for the Product API application.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ProductApiApplication {
  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(ProductApiApplication.class, args);
  }

  /**
   * Initializes the application with specific configurations.
   * Enables automatic context propagation for Reactor hooks.
   * This is useful for propagating context information in reactive streams.
   */
  @PostConstruct
  public void init() {
    Hooks.enableAutomaticContextPropagation();
  }
}
