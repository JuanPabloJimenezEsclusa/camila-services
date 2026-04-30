package com.camila.api.product.infrastructure.adapter.output.couchbase.config;

import com.couchbase.client.java.env.ClusterEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableReactiveCouchbaseRepositories;

/**
 * The type Couchbase config.
 */
@Configuration
@Conditional(CouchbaseCondition.class)
@EnableReactiveCouchbaseRepositories(basePackages = "com.camila.api.product.infrastructure.adapter.output.couchbase")
public class CouchbaseConfig extends AbstractCouchbaseConfiguration {

  @Value("#{systemEnvironment['DB_CONN_STR'] ?: environment.getProperty('spring.couchbase.connection-string', 'couchbase')}")
  private String connectionString;

  @Value("#{systemEnvironment['DB_USERNAME'] ?: environment.getProperty('spring.couchbase.username', 'Administrator')}")
  private String username;

  @Value("#{systemEnvironment['DB_PASSWORD'] ?: environment.getProperty('spring.couchbase.password', 'password')}")
  private String password;

  @Value("${spring.couchbase.bucket-name}")
  private String bucketName;

  @Value("#{systemEnvironment['DB_SSL_ENABLED'] ?: environment.getProperty('spring.couchbase.env.ssl.enabled', 'false')}")
  private String sslEnabled;

  @Override
  public String getConnectionString() {
    return this.connectionString;
  }

  @Override
  public String getUserName() {
    return this.username;
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public String getBucketName() {
    return this.bucketName;
  }

  @Bean
  @Override
  public CustomConversions customConversions() {
    return super.customConversions();
  }

  @Override
  protected void configureEnvironment(final ClusterEnvironment.Builder builder) {
    builder
      // Disable DNS SRV resolution to avoid timeouts when no SRV record exists
      .ioConfig(io -> io.enableDnsSrv(false))
      // keep existing security TLS configuration
      .securityConfig(securityBuilder -> securityBuilder.enableTls(Boolean.parseBoolean(this.sslEnabled)).build());
  }
}
