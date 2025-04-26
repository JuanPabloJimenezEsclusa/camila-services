package com.camila.api.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTag;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@ArchTag("camila-product-api")
@AnalyzeClasses(packages = "com.camila.api.product")
class ProductArchitectureTest {
  // Examples
  // https://github.com/TNG/ArchUnit/tree/main/archunit-example/example-junit5/src/test/java/com/tngtech/archunit/exampletest/junit5

  @ArchTest
  static final ArchRule LAYER_DEPENDENCIES_ARE_RESPECTED = layeredArchitecture().consideringOnlyDependenciesInLayers()
    // define layers
    .layer(HEXAGONAL_LAYERS.DOMAIN.name()).definedBy(HEXAGONAL_LAYERS.DOMAIN.getValue())
    .layer(HEXAGONAL_LAYERS.APPLICATION.name()).definedBy(HEXAGONAL_LAYERS.APPLICATION.getValue())
    .layer(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_INPUT.name()).definedBy(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_INPUT.getValue())
    .layer(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_OUTPUT.name()).definedBy(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_OUTPUT.getValue())

    // external hexagonal layers. No others layer access
    .whereLayer(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_INPUT.name()).mayNotBeAccessedByAnyLayer()
    .whereLayer(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_OUTPUT.name()).mayOnlyBeAccessedByLayers(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_INPUT.name())

    // application layer. Accessible only by externals layers
    .whereLayer(HEXAGONAL_LAYERS.APPLICATION.name()).mayOnlyBeAccessedByLayers(
      HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_INPUT.name(), HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_OUTPUT.name())
    .whereLayer(HEXAGONAL_LAYERS.APPLICATION.name()).mayOnlyAccessLayers(HEXAGONAL_LAYERS.DOMAIN.name())

    // domain layer. Not use other layer class
    .whereLayer(HEXAGONAL_LAYERS.DOMAIN.name()).mayOnlyBeAccessedByLayers(
      HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_INPUT.name(), HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_OUTPUT.name(), HEXAGONAL_LAYERS.APPLICATION.name())
    .whereLayer(HEXAGONAL_LAYERS.DOMAIN.name()).mayNotAccessAnyLayer()
    .ensureAllClassesAreContainedInArchitecture();
  @ArchTest
  static final ArchRule DOMAIN_ONLY_DEPEND_ON_STANDARD =
    classes().that().resideInAPackage(HEXAGONAL_LAYERS.DOMAIN.getValue())
      .should().onlyDependOnClassesThat().resideInAnyPackage(HEXAGONAL_LAYERS.DOMAIN.getValue(),
        // Basis dependencies
        "java..", "reactor.core..", "org.jspecify..", "com.fasterxml.jackson.annotation..");
  @ArchTest
  static final ArchRule APPLICATION_ONLY_DEPEND_ON_STANDARD_DOMAIN =
    classes().that().resideInAPackage(HEXAGONAL_LAYERS.APPLICATION.getValue())
      .should().onlyDependOnClassesThat().resideInAnyPackage(
        HEXAGONAL_LAYERS.DOMAIN.getValue(), HEXAGONAL_LAYERS.APPLICATION.getValue(),
        // Basis dependencies
        "java..", "reactor.core..", "org.jspecify..",
        // Testing dependencies
        "org.junit..", "reactor.test..", "org.mockito..", "org.assertj.core.api..", "org.instancio..");
  @ArchTest
  static final ArchRule INFRASTRUCTURE_ADAPTER_INPUT_ONLY_DEPEND_ON_STANDARD_DOMAIN_APPLICATION =
    classes().that().resideInAPackage(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_INPUT.getValue())
      .should().onlyDependOnClassesThat().resideInAnyPackage(
        HEXAGONAL_LAYERS.DOMAIN.getValue(), HEXAGONAL_LAYERS.APPLICATION.getValue(),
        HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_OUTPUT.getValue(), HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_INPUT.getValue(),
        // Basis dependencies
        "java..", "reactor.core..", "org.jspecify..", "com.fasterxml.jackson..", "org.slf4j..",
        "org.mapstruct..", "lombok..", "jakarta.validation..",
        // Spring dependencies
        "org.springframework.web..", "org.springframework.stereotype..", "org.springframework.context..",
        "org.springframework.dao..", "org.springframework.graphql..", "org.springframework.core..",
        "org.springframework.lang..", "org.springframework.messaging..", "org.springframework.http..",
        "org.springframework.data.domain..", "org.springframework.validation.annotation..",
        // GRAPHQL
        "graphql.schema..", "graphql.scalars..",
        // GRPC
        "io.grpc..", "net.devh.boot.grpc..", "com.google.protobuf..",
        // REST
        "io.swagger.v3..", "com.google.common.util.concurrent..",
        // RSocket
        "org.springframework.util..", "org.springframework.web.util.pattern..",
        // WebSocket
        "reactor.netty.http.server..",
        // Security
        "org.springframework.security.config..", "org.springframework.security.web..",
        "org.springframework.security.oauth2.server..",
        // Testing dependencies
        "org.junit..", "reactor.test..", "org.mockito..", "org.assertj.core.api..", "org.instancio..", "org.awaitility..",
        "org.springframework.test..", "org.springframework.boot.test..",
        "org.springframework.cloud..", "de.flapdoodle.embed.mongo..", "org.springframework.boot.autoconfigure..",
        // AOT
        "org.springframework.aot.generate..", "org.springframework.beans.factory..");
  @ArchTest
  static final ArchRule INFRASTRUCTURE_ADAPTER_OUTPUT_ONLY_DEPEND_ON_STANDARD_DOMAIN_APPLICATION =
    classes().that().resideInAPackage(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_OUTPUT.getValue())
      .should().onlyDependOnClassesThat().resideInAnyPackage(
        HEXAGONAL_LAYERS.DOMAIN.getValue(), HEXAGONAL_LAYERS.APPLICATION.getValue(), HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_OUTPUT.getValue(),
        // Basis dependencies
        "java..", "reactor.core..", "org.jspecify..", "org.slf4j..", "org.mapstruct..", "lombok..",
        "com.fasterxml.jackson.core..", "com.fasterxml.jackson.databind..", "jakarta.annotation..",
        // Spring dependencies
        "org.springframework.context..", "org.springframework.stereotype..", "org.springframework.boot..",
        "org.springframework.core.io..", "org.springframework.core.type..", "org.springframework.core.env..",
        // Couchbase
        "com.couchbase.client..", "com.github.dockerjava.api..",
        // MongoDB
        "org.bson..", "de.flapdoodle.embed..", "de.flapdoodle.reverse.transitions..",
        "org.springframework.data.mongodb..", "org.springframework.data.repository..", "org.springframework.data.domain..",
        // Couchbase
        "org.springframework.data.couchbase..", "org.springframework.data.annotation..",
        // Testing dependencies
        "org.junit..", "reactor.test..", "org.mockito..", "org.assertj.core.api..",
        "org.springframework.test..", "org.springframework.boot.test..", "org.testcontainers..",
        // AOT
        "org.springframework.aot.generate..", "org.springframework.beans.factory..");
  private static final String BASE_PKG = "com.camila.api.product";

  private enum HEXAGONAL_LAYERS {
    DOMAIN(BASE_PKG + ".domain.."),
    APPLICATION(BASE_PKG + ".application.."),
    INFRASTRUCTURE_ADAPTER_INPUT(BASE_PKG + ".infrastructure.adapter.input.."),
    INFRASTRUCTURE_ADAPTER_OUTPUT(BASE_PKG + ".infrastructure.adapter.output..");

    private final String value;

    HEXAGONAL_LAYERS(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
