package com.camila.api.product.infrastructure.adapter.output.mongo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Slf4j
public class MongoCondition implements Condition {

  private static final String PRIMARY_PROPERTY = "repository.technology";
  private static final String MONGO_TECHNOLOGY = "mongo";

  @Override
  public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
    final String technology = context.getEnvironment().getProperty(PRIMARY_PROPERTY, MONGO_TECHNOLOGY);
    final boolean isMongoTechnology = MONGO_TECHNOLOGY.equals(technology);

    log.debug("MongoCondition evaluated to: {}", isMongoTechnology);
    return isMongoTechnology;
  }
}
