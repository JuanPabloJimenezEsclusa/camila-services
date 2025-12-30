package com.camila.api.architecture;

import static com.camila.api.architecture.ProductArchitectureTest.HEXAGONAL_LAYERS.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTag;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

@SuppressWarnings("checkstyle:abbreviationaswordinname")
@ArchTag("camila-product-api")
@AnalyzeClasses(packages = "com.camila.api.product")
class ProductArchitectureTest {
  // Examples
  // https://github.com/TNG/ArchUnit/tree/main/archunit-example/example-junit5/src/test/java/com/tngtech/archunit/exampletest/junit5
  private static final Architectures.LayeredArchitecture LAYERED_ARCHITECTURE = Architectures.layeredArchitecture()
    .consideringOnlyDependenciesInLayers()
    .withOptionalLayers(true)
    .layer(DOMAIN.name()).definedBy(DOMAIN.path)
    .layer(APPLICATION.name()).definedBy(APPLICATION.path)
    // Infrastructure Adapter Layers - Input
    .layer(INFRASTRUCTURE_ADAPTER_INPUT_BOOT.name()).definedBy(INFRASTRUCTURE_ADAPTER_INPUT_BOOT.path)
    .layer(INFRASTRUCTURE_ADAPTER_INPUT_GRAPHQL.name()).definedBy(INFRASTRUCTURE_ADAPTER_INPUT_GRAPHQL.path)
    .layer(INFRASTRUCTURE_ADAPTER_INPUT_GRPC.name()).definedBy(INFRASTRUCTURE_ADAPTER_INPUT_GRPC.path)
    .layer(INFRASTRUCTURE_ADAPTER_INPUT_REST.name()).definedBy(INFRASTRUCTURE_ADAPTER_INPUT_REST.path)
    .layer(INFRASTRUCTURE_ADAPTER_INPUT_RSOCKET.name()).definedBy(INFRASTRUCTURE_ADAPTER_INPUT_RSOCKET.path)
    .layer(INFRASTRUCTURE_ADAPTER_INPUT_SECURITY.name()).definedBy(INFRASTRUCTURE_ADAPTER_INPUT_SECURITY.path)
    .layer(INFRASTRUCTURE_ADAPTER_INPUT_WEBSOCKET.name()).definedBy(INFRASTRUCTURE_ADAPTER_INPUT_WEBSOCKET.path)
    // Infrastructure Adapter Layers - Output
    .layer(INFRASTRUCTURE_ADAPTER_OUTPUT_CACHE.name()).definedBy(INFRASTRUCTURE_ADAPTER_OUTPUT_CACHE.path)
    .layer(INFRASTRUCTURE_ADAPTER_OUTPUT_COUCHBASE.name()).definedBy(INFRASTRUCTURE_ADAPTER_OUTPUT_COUCHBASE.path)
    .layer(INFRASTRUCTURE_ADAPTER_OUTPUT_MONGO.name()).definedBy(INFRASTRUCTURE_ADAPTER_OUTPUT_MONGO.path)
    .layer(INFRASTRUCTURE_ADAPTER_OUTPUT_UTIL.name()).definedBy(INFRASTRUCTURE_ADAPTER_OUTPUT_UTIL.path)
    .as("Hexagonal Architecture - Layered Dependencies");

  @ArchTest
  @SuppressWarnings("unused")
  private static final ArchRule DOMAIN_LAYER_DEPENDENCIES_ARE_RESPECTED = LAYERED_ARCHITECTURE
    .whereLayer(DOMAIN.name()).mayOnlyBeAccessedByLayers(
      APPLICATION.name(),
      INFRASTRUCTURE_ADAPTER_INPUT_BOOT.name(),
      INFRASTRUCTURE_ADAPTER_INPUT_GRAPHQL.name(),
      INFRASTRUCTURE_ADAPTER_INPUT_GRPC.name(),
      INFRASTRUCTURE_ADAPTER_INPUT_REST.name(),
      INFRASTRUCTURE_ADAPTER_INPUT_RSOCKET.name(),
      INFRASTRUCTURE_ADAPTER_INPUT_SECURITY.name(),
      INFRASTRUCTURE_ADAPTER_INPUT_WEBSOCKET.name(),
      INFRASTRUCTURE_ADAPTER_OUTPUT_CACHE.name(),
      INFRASTRUCTURE_ADAPTER_OUTPUT_COUCHBASE.name(),
      INFRASTRUCTURE_ADAPTER_OUTPUT_MONGO.name())
    .whereLayer(DOMAIN.name()).mayNotAccessAnyLayer()
    .ensureAllClassesAreContainedInArchitecture().as("Domain Layer - No dependencies on other layers")
    .because("the Domain Layer should be independent and not depend on any other layer");

  @ArchTest
  @SuppressWarnings("unused")
  private static final ArchRule APPLICATION_LAYER_DEPENDENCIES_ARE_RESPECTED = LAYERED_ARCHITECTURE
    .whereLayer(APPLICATION.name()).mayOnlyBeAccessedByLayers(INFRASTRUCTURE_ADAPTER_INPUT_BOOT.name())
    .whereLayer(APPLICATION.name()).mayOnlyAccessLayers(DOMAIN.name())
    .as("Application Layer - Accessible only by External Layers")
    .because("the Application Layer should only be accessed by Infrastructure Adapter Layers and depend on the Domain Layer");

  @ArchTest
  @SuppressWarnings("unused")
  private static final ArchRule INFRASTRUCTURE_LAYER_DEPENDENCIES_ARE_RESPECTED = LAYERED_ARCHITECTURE
    .whereLayer(INFRASTRUCTURE_ADAPTER_INPUT_BOOT.name()).mayNotBeAccessedByAnyLayer()
    .whereLayer(INFRASTRUCTURE_ADAPTER_INPUT_GRAPHQL.name()).mayNotBeAccessedByAnyLayer()
    .whereLayer(INFRASTRUCTURE_ADAPTER_INPUT_GRPC.name()).mayNotBeAccessedByAnyLayer()
    .whereLayer(INFRASTRUCTURE_ADAPTER_INPUT_REST.name()).mayNotBeAccessedByAnyLayer()
    .whereLayer(INFRASTRUCTURE_ADAPTER_INPUT_RSOCKET.name()).mayNotBeAccessedByAnyLayer()
    .whereLayer(INFRASTRUCTURE_ADAPTER_INPUT_SECURITY.name()).mayOnlyBeAccessedByLayers(
      INFRASTRUCTURE_ADAPTER_INPUT_GRAPHQL.name(),
      INFRASTRUCTURE_ADAPTER_INPUT_REST.name())
    .whereLayer(INFRASTRUCTURE_ADAPTER_INPUT_WEBSOCKET.name()).mayNotBeAccessedByAnyLayer()
    .whereLayer(INFRASTRUCTURE_ADAPTER_OUTPUT_CACHE.name()).mayOnlyBeAccessedByLayers(INFRASTRUCTURE_ADAPTER_INPUT_BOOT.name())
    .whereLayer(INFRASTRUCTURE_ADAPTER_OUTPUT_COUCHBASE.name()).mayOnlyBeAccessedByLayers(INFRASTRUCTURE_ADAPTER_INPUT_BOOT.name())
    .whereLayer(INFRASTRUCTURE_ADAPTER_OUTPUT_MONGO.name()).mayOnlyBeAccessedByLayers(INFRASTRUCTURE_ADAPTER_INPUT_BOOT.name())
    .as("Infrastructure Adapter Layers - External Layers")
    .because("the Infrastructure Adapter Layers should be the outermost layers and not be accessed by other layers");

  @ArchTest
  @SuppressWarnings("unused")
  private static final ArchRule DOMAIN_ONLY_DEPEND_ON_STANDARD = classes().that()
    .resideInAPackage(DOMAIN.path).should().onlyDependOnClassesThat()
    .resideInAnyPackage(DOMAIN.path,
      // Basis dependencies
      "java..", "reactor.core..", "org.jspecify..", "com.fasterxml.jackson.annotation..",
      // Testing dependencies
      "org.junit..", "reactor.test..", "org.mockito..", "org.assertj.core.api..")
    .as("Domain Layer - Only standard dependencies")
    .because("the Domain Layer should only depend on standard libraries and itself");

  @ArchTest
  @SuppressWarnings("unused")
  private static final ArchRule APPLICATION_ONLY_DEPEND_ON_STANDARD_DOMAIN = classes().that()
    .resideInAPackage(APPLICATION.path).should().onlyDependOnClassesThat()
    .resideInAnyPackage(DOMAIN.path, APPLICATION.path,
      // Basis dependencies
      "java..", "reactor.core..", "org.jspecify..", "org.slf4j..",
      // Testing dependencies
      "org.junit..", "reactor.test..", "org.mockito..", "org.assertj.core.api..", "org.instancio..")
    .as("Application Layer - Only standard and Domain dependencies")
    .because("the Application Layer should only depend on standard libraries and the Domain Layer");

  @ArchTest
  @SuppressWarnings("unused")
  private static final ArchRule INFRASTRUCTURE_ADAPTER_INPUT_BOOT_ONLY_DEPEND_ON_STANDARD_DOMAIN_APPLICATION = classes()
    .that().resideInAPackage(INFRASTRUCTURE_ADAPTER_INPUT_BOOT.path).should()
    .onlyDependOnClassesThat().resideInAnyPackage(DOMAIN.path,
      INFRASTRUCTURE_ADAPTER_INPUT_BOOT.path,
      // For UseCaseConfig, tradeoff to keep the application layer free of Spring
      APPLICATION.path,
      // For sliced spring configuration in tests too
      INFRASTRUCTURE_ADAPTER_OUTPUT_CACHE.path,
      INFRASTRUCTURE_ADAPTER_OUTPUT_COUCHBASE.path,
      INFRASTRUCTURE_ADAPTER_OUTPUT_MONGO.path,
      INFRASTRUCTURE_ADAPTER_INPUT_GRAPHQL.path,
      INFRASTRUCTURE_ADAPTER_INPUT_GRPC.path,
      INFRASTRUCTURE_ADAPTER_INPUT_REST.path,
      INFRASTRUCTURE_ADAPTER_INPUT_RSOCKET.path,
      INFRASTRUCTURE_ADAPTER_INPUT_SECURITY.path,
      INFRASTRUCTURE_ADAPTER_INPUT_WEBSOCKET.path,
      // Basis dependencies
      "java..", "reactor.core..", "org.jspecify..", "com.fasterxml.jackson..", "org.slf4j..", "org.mapstruct..", "lombok..",
      "jakarta.(validation|annotation)..",
      // Spring dependencies
      "org.springframework.(web|stereotype|context|dao|graphql|core|lang|messaging|http)..",
      "org.springframework.(validation.annotation|beans.factory)..",
      // GRAPHQL
      "graphql.(schema|scalars)..",
      // GRPC
      "io.grpc..", "net.devh.boot.grpc..", "com.google.protobuf..",
      // REST
      "io.swagger.v3..", "com.google.common.util.concurrent..",
      // RSocket
      "org.springframework.(util|web.util.pattern)..",
      // WebSocket
      "reactor.netty.http.server..",
      // Security
      "org.springframework.security.(config|web|oauth2.server)..",
      // Testing dependencies
      "org.junit..", "reactor.test..", "org.mockito..", "org.assertj.core.api..", "org.instancio..",
      "org.awaitility..", "org.springframework.test..", "org.springframework.boot.(test|autoconfigure)..",
      "org.springframework.cloud..");

  @ArchTest
  @SuppressWarnings("unused")
  private static final ArchRule INFRASTRUCTURE_ADAPTER_INPUT_GRAPHQL_ONLY_DEPEND_ON_STANDARD_DOMAIN = classes()
    .that().resideInAPackage(INFRASTRUCTURE_ADAPTER_INPUT_GRAPHQL.path).should()
    .onlyDependOnClassesThat().resideInAnyPackage(DOMAIN.path,
      INFRASTRUCTURE_ADAPTER_INPUT_GRAPHQL.path,
      INFRASTRUCTURE_ADAPTER_INPUT_SECURITY.path,
      // For UseCaseConfig, tradeoff to keep the application layer free of Spring
      APPLICATION.path,
      // Requires to slice the Spring configuration in tests too
      // For sliced spring configuration in tests too
      INFRASTRUCTURE_ADAPTER_OUTPUT_COUCHBASE.path,
      // Basis dependencies
      "java..", "org.jspecify..", "com.fasterxml.jackson..", "org.slf4j..", "org.mapstruct..", "lombok..",
      "jakarta.(validation|annotation)..",
      // Reactive dependencies
      "reactor.core..",
      // Spring dependencies
      "org.springframework.(web|stereotype|context|dao|graphql|core|lang|http)..",
      "org.springframework.(validation.annotation|beans.factory)..",
      // GRAPHQL
      "graphql.(schema|scalars)..",
      // GRPC
      "io.grpc..", "net.devh.boot.grpc..", "com.google.protobuf..",
      // Testing dependencies
      "org.junit..", "reactor.test..", "org.mockito..", "org.assertj.core.api..", "org.instancio..",
      "org.springframework.test..", "org.springframework.boot.(test|autoconfigure)..",
      "org.springframework.cloud..");

  @ArchTest
  @SuppressWarnings("unused")
  private static final ArchRule INFRASTRUCTURE_ADAPTER_INPUT_GRPC_ONLY_DEPEND_ON_STANDARD_DOMAIN = classes()
    .that().resideInAPackage(INFRASTRUCTURE_ADAPTER_INPUT_GRPC.path).should()
    .onlyDependOnClassesThat().resideInAnyPackage(DOMAIN.path,
      INFRASTRUCTURE_ADAPTER_INPUT_GRPC.path,
      // For sliced spring configuration in tests too
      INFRASTRUCTURE_ADAPTER_OUTPUT_MONGO.path,
      // Basis dependencies
      "java..", "reactor.core..", "org.jspecify..", "com.fasterxml.jackson..", "org.slf4j..", "org.mapstruct..", "lombok..",
      "jakarta.(validation|annotation)..",
      // Spring dependencies
      "org.springframework.(web|stereotype|context|dao|core|lang|messaging|http)..",
      "org.springframework.(beans.factory|validation.annotation)..",
      // GRPC
      "io.grpc..", "net.devh.boot.grpc..", "com.google.protobuf..", "com.google.common.util.concurrent..",
      // Testing dependencies
      "org.junit..", "reactor.test..", "org.mockito..", "org.assertj.core.api..", "org.instancio..",
      "org.springframework.test..", "org.springframework.boot.(test|autoconfigure)..");

  @ArchTest
  @SuppressWarnings("unused")
  private static final ArchRule INFRASTRUCTURE_ADAPTER_INPUT_REST_ONLY_DEPEND_ON_STANDARD_DOMAIN = classes()
    .that().resideInAPackage(INFRASTRUCTURE_ADAPTER_INPUT_REST.path).should()
    .onlyDependOnClassesThat().resideInAnyPackage(DOMAIN.path,
      INFRASTRUCTURE_ADAPTER_INPUT_REST.path,
      INFRASTRUCTURE_ADAPTER_INPUT_SECURITY.path,
      // For UseCaseConfig, tradeoff to keep the application layer free of Spring
      APPLICATION.path,
      // For sliced spring configuration in tests too
      INFRASTRUCTURE_ADAPTER_OUTPUT_COUCHBASE.path,
      // Basis dependencies
      "java..", "reactor.core..", "org.jspecify..", "com.fasterxml.jackson..", "org.slf4j..", "org.mapstruct..", "lombok..",
      "jakarta.(validation|annotation)..",
      // Spring dependencies
      "org.springframework.(web|stereotype|context|dao|graphql|core|lang|messaging|http)..",
      "org.springframework.(data.domain|validation.annotation|beans.factory)..",
      // GRPC
      "io.grpc..", "net.devh.boot.grpc..", "com.google.protobuf..",
      // REST
      "io.swagger.v3..", "com.google.common.util.concurrent..",
      // Testing dependencies
      "org.junit..", "reactor.test..", "org.mockito..", "org.assertj.core.api..", "org.instancio..",
      "org.springframework.test..", "org.springframework.boot.(test|autoconfigure)..",
      "org.springframework.cloud..");

  @ArchTest
  @SuppressWarnings("unused")
  private static final ArchRule INFRASTRUCTURE_ADAPTER_INPUT_RSOCKET_ONLY_DEPEND_ON_STANDARD_DOMAIN = classes()
    .that().resideInAPackage(INFRASTRUCTURE_ADAPTER_INPUT_RSOCKET.path).should()
    .onlyDependOnClassesThat().resideInAnyPackage(DOMAIN.path,
      INFRASTRUCTURE_ADAPTER_INPUT_RSOCKET.path,
      // For sliced spring configuration in tests too
      INFRASTRUCTURE_ADAPTER_OUTPUT_MONGO.path,
      // Basis dependencies
      "java..", "reactor.core..", "org.jspecify..", "com.fasterxml.jackson..", "org.slf4j..", "lombok..",
      // Spring dependencies
      "org.springframework.(web|stereotype|context|core|lang|messaging|http|beans.factory)..",
      // RSocket
      "org.springframework.(util|web.util.pattern)..",
      // Testing dependencies
      "org.junit..", "reactor.test..", "org.mockito..", "org.assertj.core.api..", "org.instancio..",
      "org.springframework.test..", "org.springframework.boot.(test|autoconfigure)..");

  @ArchTest
  @SuppressWarnings("unused")
  private static final ArchRule INFRASTRUCTURE_ADAPTER_INPUT_SECURITY_ONLY_DEPEND_ON_STANDARD_DOMAIN = classes()
    .that().resideInAPackage(INFRASTRUCTURE_ADAPTER_INPUT_SECURITY.path).should()
    .onlyDependOnClassesThat().resideInAnyPackage(DOMAIN.path,
      INFRASTRUCTURE_ADAPTER_INPUT_SECURITY.path,
      // Basis dependencies
      "java..", "reactor.core..", "org.jspecify..",
      // Spring dependencies
      "org.springframework.(web|stereotype|context|core|http|beans.factory)..",
      // Security
      "org.springframework.security.(config|web|oauth2.server)..");

  @ArchTest
  @SuppressWarnings("unused")
  private static final ArchRule INFRASTRUCTURE_ADAPTER_INPUT_WEBSOCKET_ONLY_DEPEND_ON_STANDARD_DOMAIN = classes()
    .that().resideInAPackage(INFRASTRUCTURE_ADAPTER_INPUT_WEBSOCKET.path).should()
    .onlyDependOnClassesThat().resideInAnyPackage(DOMAIN.path,
      INFRASTRUCTURE_ADAPTER_INPUT_WEBSOCKET.path,
      // For sliced spring configuration in tests
      INFRASTRUCTURE_ADAPTER_OUTPUT_MONGO.path,
      // Basis dependencies
      "java..", "reactor.core..", "org.jspecify..", "com.fasterxml.jackson..", "org.slf4j..", "lombok..",
      // Spring dependencies
      "org.springframework.(web|stereotype|context|core|http|beans.factory)..",
      // WebSocket
      "reactor.netty.http.server..",
      // Testing dependencies
      "org.junit..", "reactor.test..", "org.mockito..", "org.assertj.core.api..", "org.instancio..",
      "org.springframework.test..", "org.springframework.boot.(test|autoconfigure)..");

  @ArchTest
  @SuppressWarnings("unused")
  private static final ArchRule INFRASTRUCTURE_ADAPTER_OUTPUT_CACHE_ONLY_DEPEND_ON_STANDARD_DOMAIN = classes()
    .that().resideInAPackage(INFRASTRUCTURE_ADAPTER_OUTPUT_CACHE.path).should()
    .onlyDependOnClassesThat().resideInAnyPackage(DOMAIN.path,
      INFRASTRUCTURE_ADAPTER_OUTPUT_CACHE.path,
      // Basis dependencies
      "java..", "reactor.core..", "org.jspecify..", "org.slf4j..", "lombok..",
      // Spring dependencies
      "org.springframework.(context|stereotype|cache|beans.factory)..", "org.springframework.core.(io|type|env)..",
      // Cache
      "com.github.benmanes.caffeine..", "org.springframework.data.redis..",
      // Testing dependencies
      "org.junit..", "reactor.test..", "org.mockito..", "org.assertj.core.api..", "org.instancio..",
      "org.springframework.test..", "org.testcontainers..", "com.redis.testcontainers..", "com.github.dockerjava.api..");

  @ArchTest
  @SuppressWarnings("unused")
  private static final ArchRule INFRASTRUCTURE_ADAPTER_OUTPUT_COUCHBASE_ONLY_DEPEND_ON_STANDARD_DOMAIN = classes()
    .that().resideInAPackage(INFRASTRUCTURE_ADAPTER_OUTPUT_COUCHBASE.path).should()
    .onlyDependOnClassesThat().resideInAnyPackage(DOMAIN.path,
      INFRASTRUCTURE_ADAPTER_OUTPUT_COUCHBASE.path,
      // Basis dependencies
      "java..", "reactor.core..", "org.jspecify..", "org.slf4j..", "org.mapstruct..", "lombok..",
      "com.fasterxml.jackson.(core|databind)..",
      // Spring dependencies
      "org.springframework.(context|stereotype|beans.factory)..", "org.springframework.core.(io|type|env)..",
      "org.springframework.boot..",
      // Couchbase
      "com.couchbase.client..", "org.springframework.data.(annotation|couchbase|convert|repository)..",
      // Testing dependencies
      "org.junit..", "reactor.test..", "org.mockito..", "org.assertj.core.api..", "org.instancio..",
      "org.springframework.test..", "org.springframework.boot.test..", "org.testcontainers..", "com.github.dockerjava.api..");

  @ArchTest
  @SuppressWarnings("unused")
  private static final ArchRule INFRASTRUCTURE_ADAPTER_OUTPUT_MONGO_ONLY_DEPEND_ON_STANDARD_DOMAIN = classes()
    .that().resideInAPackage(INFRASTRUCTURE_ADAPTER_OUTPUT_MONGO.path).should()
    .onlyDependOnClassesThat().resideInAnyPackage(DOMAIN.path,
      INFRASTRUCTURE_ADAPTER_OUTPUT_MONGO.path,
      // Basis dependencies
      "java..", "reactor.core..", "org.jspecify..", "org.slf4j..", "org.mapstruct..", "lombok..",
      "com.fasterxml.jackson.(core|databind)..",
      // Spring dependencies
      "org.springframework.(context|stereotype|beans.factory)..", "org.springframework.core.(io|type|env)..",
      "org.springframework.boot..",
      // MongoDB
      "org.bson..", "com.mongodb.client..", "org.springframework.data.(annotation|domain|mongodb|repository)..",
      // Testing dependencies
      "org.junit..", "reactor.test..", "org.mockito..", "org.assertj.core.api..", "org.instancio..",
      "org.springframework.test..", "org.springframework.boot.test..", "org.testcontainers..", "com.github.dockerjava.api..");

  @ArchTest
  @SuppressWarnings("unused")
  private static final ArchRule INFRASTRUCTURE_ADAPTER_OUTPUT_UTIL_ONLY_DEPEND_ON_STANDARD = classes()
    .that().resideInAPackage(INFRASTRUCTURE_ADAPTER_OUTPUT_UTIL.path).should()
    .onlyDependOnClassesThat().resideInAnyPackage("java..");

  enum HEXAGONAL_LAYERS {
    BASE_PKG("com.camila.api.product"),
    DOMAIN(BASE_PKG.path + ".domain.."),
    APPLICATION(BASE_PKG.path + ".application.."),
    INFRASTRUCTURE_ADAPTER_INPUT_BOOT(BASE_PKG.path + ".infrastructure.adapter.input.."),
    INFRASTRUCTURE_ADAPTER_INPUT_GRAPHQL(BASE_PKG.path + ".infrastructure.adapter.input.graphql.."),
    INFRASTRUCTURE_ADAPTER_INPUT_GRPC(BASE_PKG.path + ".infrastructure.adapter.input.grpc.."),
    INFRASTRUCTURE_ADAPTER_INPUT_REST(BASE_PKG.path + ".infrastructure.adapter.input.rest.."),
    INFRASTRUCTURE_ADAPTER_INPUT_RSOCKET(BASE_PKG.path + ".infrastructure.adapter.input.rsocket.."),
    INFRASTRUCTURE_ADAPTER_INPUT_SECURITY(BASE_PKG.path + ".infrastructure.adapter.input.security.."),
    INFRASTRUCTURE_ADAPTER_INPUT_WEBSOCKET(BASE_PKG.path + ".infrastructure.adapter.input.websocket.."),
    INFRASTRUCTURE_ADAPTER_OUTPUT_CACHE(BASE_PKG.path + ".infrastructure.adapter.output.cache.."),
    INFRASTRUCTURE_ADAPTER_OUTPUT_COUCHBASE(BASE_PKG.path + ".infrastructure.adapter.output.couchbase.."),
    INFRASTRUCTURE_ADAPTER_OUTPUT_MONGO(BASE_PKG.path + ".infrastructure.adapter.output.mongo.."),
    INFRASTRUCTURE_ADAPTER_OUTPUT_UTIL(BASE_PKG.path + ".infrastructure.adapter.output.util..");

    private final String path;

    HEXAGONAL_LAYERS(String path) {
      this.path = path;
    }
  }
}
