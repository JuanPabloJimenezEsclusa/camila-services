package com.camila.api.product.infrastructure.adapter.output.mongo.config;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

/**
 * The type Mongo config.
 */
@Configuration
@Conditional(MongoCondition.class)
@EnableReactiveMongoRepositories(basePackages = "com.camila.api.product.infrastructure.adapter.output.mongo")
public class MongoConfig {
}
