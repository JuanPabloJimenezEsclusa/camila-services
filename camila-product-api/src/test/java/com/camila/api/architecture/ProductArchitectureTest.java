package com.camila.api.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTag;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

@ArchTag("camila-product-api")
@AnalyzeClasses(packages = "com.camila.api.product")
class ProductArchitectureTest {
	// Examples
	// https://github.com/TNG/ArchUnit/tree/main/archunit-example/example-junit5/src/test/java/com/tngtech/archunit/exampletest/junit5
	private static final Architectures.LayeredArchitecture LAYERED_ARCHITECTURE = Architectures.layeredArchitecture()
			.consideringOnlyDependenciesInLayers().layer(HEXAGONAL_LAYERS.DOMAIN.name())
			.definedBy(HEXAGONAL_LAYERS.DOMAIN.path).layer(HEXAGONAL_LAYERS.APPLICATION.name())
			.definedBy(HEXAGONAL_LAYERS.APPLICATION.path).layer(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_INPUT.name())
			.definedBy(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_INPUT.path)
			.layer(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_OUTPUT.name())
			.definedBy(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_OUTPUT.path)
			.as("Hexagonal Architecture - Layered Dependencies");

	@ArchTest
	@SuppressWarnings("unused")
	private static final ArchRule DOMAIN_LAYER_DEPENDENCIES_ARE_RESPECTED = LAYERED_ARCHITECTURE
			.whereLayer(HEXAGONAL_LAYERS.DOMAIN.name())
			.mayOnlyBeAccessedByLayers(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_INPUT.name(),
					HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_OUTPUT.name(), HEXAGONAL_LAYERS.APPLICATION.name())
			.whereLayer(HEXAGONAL_LAYERS.DOMAIN.name()).mayNotAccessAnyLayer()
			.ensureAllClassesAreContainedInArchitecture().as("Domain Layer - No dependencies on other layers")
			.because("the Domain Layer should be independent and not depend on any other layer");

	@ArchTest
	@SuppressWarnings("unused")
	private static final ArchRule APPLICATION_LAYER_DEPENDENCIES_ARE_RESPECTED = LAYERED_ARCHITECTURE
			.whereLayer(HEXAGONAL_LAYERS.APPLICATION.name())
			.mayOnlyBeAccessedByLayers(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_INPUT.name(),
					HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_OUTPUT.name())
			.whereLayer(HEXAGONAL_LAYERS.APPLICATION.name()).mayOnlyAccessLayers(HEXAGONAL_LAYERS.DOMAIN.name())
			.as("Application Layer - Accessible only by External Layers").because(
					"the Application Layer should only be accessed by Infrastructure Adapter Layers and depend on the Domain Layer");

	@ArchTest
	@SuppressWarnings("unused")
	private static final ArchRule INFRASTRUCTURE_LAYER_DEPENDENCIES_ARE_RESPECTED = LAYERED_ARCHITECTURE
			.whereLayer(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_INPUT.name()).mayNotBeAccessedByAnyLayer()
			.whereLayer(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_OUTPUT.name())
			.mayOnlyBeAccessedByLayers(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_INPUT.name())
			.as("Infrastructure Adapter Layers - External Layers").because(
					"the Infrastructure Adapter Layers should be the outermost layers and not be accessed by other layers");

	@ArchTest
	@SuppressWarnings("unused")
	private static final ArchRule DOMAIN_ONLY_DEPEND_ON_STANDARD = classes().that()
			.resideInAPackage(HEXAGONAL_LAYERS.DOMAIN.path).should().onlyDependOnClassesThat()
			.resideInAnyPackage(HEXAGONAL_LAYERS.DOMAIN.path,
					// Basis dependencies
					"java..", "reactor.core..", "org.jspecify..", "com.fasterxml.jackson.annotation..")
			.as("Domain Layer - Only standard dependencies")
			.because("the Domain Layer should only depend on standard libraries and itself");

	@ArchTest
	@SuppressWarnings("unused")
	private static final ArchRule APPLICATION_ONLY_DEPEND_ON_STANDARD_DOMAIN = classes().that()
			.resideInAPackage(HEXAGONAL_LAYERS.APPLICATION.path).should().onlyDependOnClassesThat()
			.resideInAnyPackage(HEXAGONAL_LAYERS.DOMAIN.path, HEXAGONAL_LAYERS.APPLICATION.path,
					// Basis dependencies
					"java..", "reactor.core..", "org.jspecify..", "org.slf4j..",
					// Testing dependencies
					"org.junit..", "reactor.test..", "org.mockito..", "org.assertj.core.api..", "org.instancio..")
			.as("Application Layer - Only standard and Domain dependencies")
			.because("the Application Layer should only depend on standard libraries and the Domain Layer");

	@ArchTest
	@SuppressWarnings("unused")
	private static final ArchRule INFRASTRUCTURE_ADAPTER_INPUT_ONLY_DEPEND_ON_STANDARD_DOMAIN_APPLICATION = classes()
			.that().resideInAPackage(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_INPUT.path).should()
			.onlyDependOnClassesThat().resideInAnyPackage(HEXAGONAL_LAYERS.DOMAIN.path,
					// For UseCaseConfig, tradeoff to keep the application layer free of Spring
					// dependencies
					// Requires to slice the Spring configuration in tests too
					HEXAGONAL_LAYERS.APPLICATION.path,
					// For sliced spring configuration in tests too
					HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_OUTPUT.path,
					HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_INPUT.path,
					// Basis dependencies
					"java..", "org.jspecify..", "com.fasterxml.jackson..", "org.slf4j..", "org.mapstruct..", "lombok..",
					"jakarta.(validation|annotation)..",
					// Reactive dependencies
					"reactor.(core|util.context)..", "org.reactivestreams..",
					// Spring dependencies
					"org.springframework.(web|stereotype|context|dao|graphql|core|lang|messaging|http)..",
					"org.springframework.(data.domain|validation.annotation)..",
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
					"org.springframework.cloud..",
					// AOT
					"org.springframework.aot.generate..", "org.springframework.beans.factory..");

	@ArchTest
	@SuppressWarnings("unused")
	private static final ArchRule INFRASTRUCTURE_ADAPTER_OUTPUT_ONLY_DEPEND_ON_STANDARD_DOMAIN_APPLICATION = classes()
			.that().resideInAPackage(HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_OUTPUT.path).should()
			.onlyDependOnClassesThat().resideInAnyPackage(HEXAGONAL_LAYERS.DOMAIN.path,
					HEXAGONAL_LAYERS.INFRASTRUCTURE_ADAPTER_OUTPUT.path,
					// Basis dependencies
					"java..", "reactor.core..", "org.jspecify..", "org.slf4j..", "org.mapstruct..", "lombok..",
					"com.fasterxml.jackson.(core|databind)..", "jakarta.annotation..",
					// Spring dependencies
					"org.springframework.(context|stereotype|cache)..", "org.springframework.core.(io|type|env)..",
					"org.springframework.boot..",
					// Cache
					"com.github.benmanes.caffeine..", "org.springframework.data.redis..",
					// MongoDB
					"org.bson..", "com.mongodb.client..", "org.springframework.data.(domain|mongodb|repository)..",
					// Couchbase
					"com.couchbase.client..", "com.github.dockerjava.api..",
					"org.springframework.data.(annotation|couchbase|convert)..",
					// Testing dependencies
					"org.junit..", "reactor.test..", "org.mockito..", "org.assertj.core.api..", "org.instancio..",
					"org.springframework.test..", "org.springframework.boot.test..", "org.testcontainers..",
					"com.redis.testcontainers..", "okhttp3",
					// AOT
					"org.springframework.aot.generate..", "org.springframework.beans.factory..");

	private enum HEXAGONAL_LAYERS {
		BASE_PKG("com.camila.api.product"), //
		DOMAIN(BASE_PKG.path + ".domain.."), //
		APPLICATION(BASE_PKG.path + ".application.."), //
		INFRASTRUCTURE_ADAPTER_INPUT(BASE_PKG.path + ".infrastructure.adapter.input.."), //
		INFRASTRUCTURE_ADAPTER_OUTPUT(BASE_PKG.path + ".infrastructure.adapter.output..");

		private final String path;

		HEXAGONAL_LAYERS(String path) {
			this.path = path;
		}
	}
}
