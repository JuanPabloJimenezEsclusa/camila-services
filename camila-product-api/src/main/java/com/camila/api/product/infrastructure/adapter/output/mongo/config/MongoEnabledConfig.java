package com.camila.api.product.infrastructure.adapter.output.mongo.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseAutoConfiguration;
import org.springframework.boot.autoconfigure.data.couchbase.CouchbaseDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.couchbase.CouchbaseReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.couchbase.CouchbaseReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.couchbase.CouchbaseRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

/**
 * The type Mongo enabled config.
 */
@Configuration
@Profile("loc|local-compose|int|dev|pre|pro")
@Conditional(MongoCondition.class)
@EnableReactiveMongoRepositories(basePackages = "com.camila.api.product.infrastructure.adapter.output.mongo")
@EnableAutoConfiguration(exclude = {
  CouchbaseAutoConfiguration.class,
  CouchbaseDataAutoConfiguration.class,
  CouchbaseRepositoriesAutoConfiguration.class,
  CouchbaseReactiveDataAutoConfiguration.class,
  CouchbaseReactiveRepositoriesAutoConfiguration.class
})
public class MongoEnabledConfig {
}
