package com.camila.api.product.infrastructure.adapter.output.couchbase.config;

import com.couchbase.client.core.deps.com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * A condition that determines whether Couchbase should be enabled based on the application configuration.
 *
 * <p>This condition checks the value of the "repository.technology" property in the application's environment.
 * If the property is set to "couchbase", the condition evaluates to true, enabling Couchbase-specific configurations.
 * Otherwise, it defaults to "mongo" and evaluates to false.</p>
 */
@Slf4j
public class CouchbaseCondition implements Condition {

  private static final String PRIMARY_PROPERTY = "repository.technology";
  private static final String DEFAULT_TECHNOLOGY = "mongo";
  private static final String COUCHBASE_TECHNOLOGY = "couchbase";

  @Override
  public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
    final boolean isCouchbaseTechnology = Optional
      .of(context.getEnvironment().getProperty(PRIMARY_PROPERTY, DEFAULT_TECHNOLOGY))
      .get().matches(COUCHBASE_TECHNOLOGY);

    log.debug("CouchbaseCondition evaluated to: {}", isCouchbaseTechnology);
    return isCouchbaseTechnology;
  }
}
