package com.camila.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The type Camila admin application.
 */
@EnableScheduling
@EnableAdminServer
@EnableDiscoveryClient
@SpringBootApplication
@SuppressWarnings("PMD.ClassWithOnlyPrivateConstructorsShouldBeFinal") // Because Spring requires a non-final class for proxying
class CamilaAdminApplication {

  private CamilaAdminApplication() {}

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(CamilaAdminApplication.class, args);
  }
}
