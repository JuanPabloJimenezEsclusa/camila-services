package com.camila.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * The type Camila config application.
 */
@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
public class CamilaConfigApplication {

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(CamilaConfigApplication.class, args);
  }
}
