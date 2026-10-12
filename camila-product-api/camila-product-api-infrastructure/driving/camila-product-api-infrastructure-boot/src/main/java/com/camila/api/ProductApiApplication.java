package com.camila.api;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@EnableDiscoveryClient
public class ProductApiApplication {

  static void main(String[] args) {
    SpringApplication.run(ProductApiApplication.class, args);
  }

  @PostConstruct
  public void init() {
    Hooks.enableAutomaticContextPropagation();
  }
}
