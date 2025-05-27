package com.camila.api.product.infrastructure.adapter.output.couchbase.config;

import com.couchbase.client.java.env.ClusterEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.mongo.MongoMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableReactiveCouchbaseRepositories;

/**
 * The type Couchbase config.
 */
@Configuration
@Conditional(CouchbaseCondition.class)
@EnableReactiveCouchbaseRepositories(basePackages = "com.camila.api.product.infrastructure.adapter.output.couchbase")
@EnableAutoConfiguration(exclude = {
  // MongoDB autoconfiguration must be excluded to avoid conflicts
  MongoAutoConfiguration.class,
  MongoMetricsAutoConfiguration.class,
  MongoReactiveAutoConfiguration.class,
  MongoRepositoriesAutoConfiguration.class,
  MongoDataAutoConfiguration.class,
  MongoReactiveDataAutoConfiguration.class
})
public class CouchbaseConfig extends AbstractCouchbaseConfiguration {

  @Value("#{systemEnvironment['DB_CONN_STR'] ?: environment.getProperty('spring.couchbase.connection-string', 'couchbase')}")
  private String connectionString;

  @Value("#{systemEnvironment['DB_USERNAME'] ?: environment.getProperty('spring.couchbase.username', 'Administrator')}")
  private String username;

  @Value("#{systemEnvironment['DB_PASSWORD'] ?: environment.getProperty('spring.couchbase.password', 'password')}")
  private String password;

  @Value("${spring.data.couchbase.bucket-name}")
  private String bucketName;

  @Value("#{systemEnvironment['DB_SSL_ENABLED'] ?: environment.getProperty('spring.couchbase.env.ssl.enabled', 'false')}")
  private String sslEnabled;

  @Override
  public String getConnectionString() {
    return connectionString;
  }

  @Override
  public String getUserName() {
    return username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getBucketName() {
    return bucketName;
  }

  @Override
  protected void configureEnvironment(final ClusterEnvironment.Builder builder) {
    builder.securityConfig(securityBuilder ->
      securityBuilder.enableTls(Boolean.parseBoolean(sslEnabled))
        .build());
  }
}
