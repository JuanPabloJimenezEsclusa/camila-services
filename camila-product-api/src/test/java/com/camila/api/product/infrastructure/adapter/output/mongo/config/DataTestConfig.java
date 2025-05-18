package com.camila.api.product.infrastructure.adapter.output.mongo.config;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Component;

/**
 * The type Data test config.
 */
@Slf4j
@Component
@ConditionalOnMissingBean(MongoConfig.class)
@EnableAutoConfiguration
public class DataTestConfig {
  private static final String EMBEDDED_DDL_XML = "classpath:/scripts/mongo/products.json";

  private final ReactiveMongoOperations mongoOperations;

  @Value(EMBEDDED_DDL_XML)
  private Resource resourceFile;

  /**
   * Instantiates a new Data test config.
   *
   * @param mongoOperations the mongo operations
   */
  public DataTestConfig(final ReactiveMongoOperations mongoOperations) {
    this.mongoOperations = mongoOperations;
  }

  /**
   * Init.
   *
   * @throws IOException the io exception
   */
  @PostConstruct
  public void init() throws IOException {
    final var file = resourceFile.getFile();
    final var jsonQuery = Files.readString(file.toPath());
    final var operationResult = mongoOperations.executeCommand(jsonQuery);

    operationResult
      .doOnNext(result -> log.info("Data test result: {}", Objects.requireNonNull(result).toJson()))
      .subscribe();
  }
}
