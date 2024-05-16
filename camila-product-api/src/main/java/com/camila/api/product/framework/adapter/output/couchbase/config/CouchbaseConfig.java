package com.camila.api.product.framework.adapter.output.couchbase.config;

import com.couchbase.client.core.error.UnambiguousTimeoutException;
import com.couchbase.client.java.ReactiveCluster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * The type Couchbase config.
 */
@Configuration
@Slf4j
class CouchbaseConfig {
  @Value("#{systemEnvironment['DB_CONN_STR'] ?: '${spring.couchbase.connection-string:local}'}")
  private String connectionString;

  @Value("#{systemEnvironment['DB_USERNAME'] ?: '${spring.couchbase.username:admin}'}")
  private String username;

  @Value("#{systemEnvironment['DB_PASSWORD'] ?: '${spring.couchbase.password:admin}'}")
  private String password;

  /**
   * Gets couchbase cluster.
   *
   * @return the couchbase cluster
   */
  @Bean(destroyMethod = "disconnect")
  ReactiveCluster getCouchbaseCluster() {
    try {
      log.debug("Connecting to Couchbase cluster at {}", connectionString);
      var cluster = ReactiveCluster.connect(connectionString, username, password);
      cluster.waitUntilReady(Duration.ofSeconds(15));
      return cluster;
    } catch (UnambiguousTimeoutException e) {
      log.error("Connection to Couchbase cluster at {} timed out", connectionString);
      throw e;
    } catch (Exception e) {
      log.error(e.getClass().getName());
      log.error("Could not connect to Couchbase cluster at {}", connectionString);
      throw e;
    }
  }
}
