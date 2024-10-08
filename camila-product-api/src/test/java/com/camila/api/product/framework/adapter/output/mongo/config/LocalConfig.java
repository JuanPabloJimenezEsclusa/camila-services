package com.camila.api.product.framework.adapter.output.mongo.config;

import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.spring.autoconfigure.TypedBeanPostProcessor;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.process.io.ProcessOutput;
import de.flapdoodle.reverse.transitions.Start;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

/**
 * The type Local config.
 */
@Configuration
@EnableAutoConfiguration
@AutoConfigureDataMongo
@EnableReactiveMongoRepositories(basePackages = "com.camila.api.product.framework.adapter.output.mongo")
public class LocalConfig {
  /**
   * Customize mongod bean post processor.
   *
   * @return the bean post processor
   */
  @Bean
  BeanPostProcessor customizeMongod() {
    return TypedBeanPostProcessor.applyBeforeInitialization(Mongod.class, src -> Mongod.builder()
      .from(src)
      .mongodArguments(Start.to(MongodArguments.class)
        .initializedWith(MongodArguments.defaults()
          .withAuth(false)
          .withSyncDelay(10) // seconds
          .withUseNoPrealloc(false)
          .withUseSmallFiles(false)
          .withUseNoJournal(false)
          .withEnableTextSearch(true)
          .withStorageEngine("wiredTiger")))
      .processOutput(Start.to(ProcessOutput.class)
        .initializedWith(ProcessOutput.namedConsole("custom")))
      .build());
  }
}
