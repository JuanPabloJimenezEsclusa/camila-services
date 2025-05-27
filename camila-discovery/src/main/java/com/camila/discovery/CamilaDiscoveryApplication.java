package com.camila.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * The type Camila discovery application.
 */
@SpringBootApplication
@EnableEurekaServer
public class CamilaDiscoveryApplication {

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(CamilaDiscoveryApplication.class, args);
  }
}
