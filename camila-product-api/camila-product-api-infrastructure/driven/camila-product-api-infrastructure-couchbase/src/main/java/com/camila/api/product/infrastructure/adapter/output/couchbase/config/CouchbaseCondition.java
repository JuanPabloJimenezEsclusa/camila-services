package com.camila.api.product.infrastructure.adapter.output.couchbase.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Slf4j
public class CouchbaseCondition implements Condition {

  private static final String PRIMARY_PROPERTY = "repository.technology";
  private static final String DEFAULT_TECHNOLOGY = "mongo";
  private static final String COUCHBASE_TECHNOLOGY = "couchbase";

  @Override
  public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
    final String technology = context.getEnvironment().getProperty(PRIMARY_PROPERTY, DEFAULT_TECHNOLOGY);
    final boolean isCouchbaseTechnology = COUCHBASE_TECHNOLOGY.equals(technology);

    log.debug("CouchbaseCondition evaluated to: {}", isCouchbaseTechnology);
    return isCouchbaseTechnology;
  }
}
