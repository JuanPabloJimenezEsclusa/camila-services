package com.camila.api.product.framework.adapter.output.repository.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Objects;

/**
 * The type Data test config.
 */
@Slf4j
@Component
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
  public DataTestConfig(ReactiveMongoOperations mongoOperations) {
    this.mongoOperations = mongoOperations;
  }

  /**
   * Init.
   *
   * @throws IOException the io exception
   */
  @PostConstruct
  public void init() throws IOException  {
    File file = resourceFile.getFile();
    var jsonQuery  = Files.readString(file.toPath());
    var documentMono = mongoOperations.executeCommand(jsonQuery);
    log.info(Objects.requireNonNull(documentMono.block(Duration.ofSeconds(10L))).toJson());
  }
}
