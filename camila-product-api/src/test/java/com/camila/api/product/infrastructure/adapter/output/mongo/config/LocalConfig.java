package com.camila.api.product.infrastructure.adapter.output.mongo.config;

import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.spring.autoconfigure.TypedBeanPostProcessor;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.process.io.ProcessOutput;
import de.flapdoodle.reverse.transitions.Start;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@Profile("default|test")
@ConditionalOnMissingBean(MongoEnabledConfig.class)
@EnableAutoConfiguration
@AutoConfigureDataMongo
@EnableReactiveMongoRepositories(basePackages = "com.camila.api.product.infrastructure.adapter.output.mongo")
public class LocalConfig {

  @Bean
  BeanPostProcessor customizeMongod() {
    return TypedBeanPostProcessor.applyBeforeInitialization(Mongod.class, src -> Mongod.builder()
        .from(src)
        .mongodArguments(Start.to(MongodArguments.class)
          .initializedWith(MongodArguments.defaults()
            .withAuth(false)
            .withSyncDelay(1)
            .withUseNoPrealloc(false)
            .withUseSmallFiles(false)
            .withUseNoJournal(true)
            .withEnableTextSearch(true)
            .withStorageEngine("wiredTiger")))
        .processOutput(Start.to(ProcessOutput.class)
          .initializedWith(ProcessOutput.namedConsole("custom")))
        .build());
  }
}
