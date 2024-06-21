package com.camila.api.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTag;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@ArchTag("camila-product-api")
@AnalyzeClasses(packages = "com.camila.api.product")
class ProductArchitectureTest {
  // Examples
  // https://github.com/TNG/ArchUnit/tree/main/archunit-example/example-junit5/src/test/java/com/tngtech/archunit/exampletest/junit5

  private static final String BASE_PKG = "com.camila.api.product";
  private enum HEXAGONAL_LAYERS {
    DOMAIN(BASE_PKG + ".domain.."),
    APPLICATION(BASE_PKG + ".application.."),
    FRAMEWORK_ADAPTER_INPUT(BASE_PKG + ".framework.adapter.input.."),
    FRAMEWORK_ADAPTER_OUTPUT(BASE_PKG + ".framework.adapter.output..");

    private final String value;

    public String getValue() {
      return value;
    }

    HEXAGONAL_LAYERS(String value) {
      this.value = value;
    }
  }

  @ArchTest
  static final ArchRule LAYER_DEPENDENCIES_ARE_RESPECTED = layeredArchitecture().consideringOnlyDependenciesInLayers()
    // define layers
    .layer(HEXAGONAL_LAYERS.DOMAIN.name()).definedBy(HEXAGONAL_LAYERS.DOMAIN.getValue())
    .layer(HEXAGONAL_LAYERS.APPLICATION.name()).definedBy(HEXAGONAL_LAYERS.APPLICATION.getValue())
    .layer(HEXAGONAL_LAYERS.FRAMEWORK_ADAPTER_INPUT.name()).definedBy(HEXAGONAL_LAYERS.FRAMEWORK_ADAPTER_INPUT.getValue())
    .layer(HEXAGONAL_LAYERS.FRAMEWORK_ADAPTER_OUTPUT.name()).definedBy(HEXAGONAL_LAYERS.FRAMEWORK_ADAPTER_OUTPUT.getValue())

    // external hexagonal layers. No others layer access
    .whereLayer(HEXAGONAL_LAYERS.FRAMEWORK_ADAPTER_INPUT.name()).mayNotBeAccessedByAnyLayer()
    .whereLayer(HEXAGONAL_LAYERS.FRAMEWORK_ADAPTER_OUTPUT.name()).mayNotBeAccessedByAnyLayer()

    // application layer. Accessible only by externals layers
    .whereLayer(HEXAGONAL_LAYERS.APPLICATION.name()).mayOnlyBeAccessedByLayers(
      HEXAGONAL_LAYERS.FRAMEWORK_ADAPTER_INPUT.name(), HEXAGONAL_LAYERS.FRAMEWORK_ADAPTER_OUTPUT.name())
    .whereLayer(HEXAGONAL_LAYERS.APPLICATION.name()).mayOnlyAccessLayers(HEXAGONAL_LAYERS.DOMAIN.name())

    // domain layer. Not use other layer class
    .whereLayer(HEXAGONAL_LAYERS.DOMAIN.name()).mayOnlyBeAccessedByLayers(
      HEXAGONAL_LAYERS.FRAMEWORK_ADAPTER_INPUT.name(), HEXAGONAL_LAYERS.FRAMEWORK_ADAPTER_OUTPUT.name(), HEXAGONAL_LAYERS.APPLICATION.name())
    .whereLayer(HEXAGONAL_LAYERS.DOMAIN.name()).mayNotAccessAnyLayer()
    .ensureAllClassesAreContainedInArchitecture();

  @ArchTest
  static final ArchRule DOMAIN_ONLY_DEPEND_ON_STANDARD =
    classes().that().resideInAPackage(HEXAGONAL_LAYERS.DOMAIN.getValue())
      .should().onlyDependOnClassesThat().resideInAnyPackage(HEXAGONAL_LAYERS.DOMAIN.getValue(),
        // Basis dependencies
        "java..", "reactor.core..",
        // Spring dependencies
        "org.springframework.lang..", "com.fasterxml.jackson.annotation..");

  @ArchTest
  static final ArchRule APPLICATION_ONLY_DEPEND_ON_STANDARD_DOMAIN =
    classes().that().resideInAPackage(HEXAGONAL_LAYERS.APPLICATION.getValue())
      .should().onlyDependOnClassesThat().resideInAnyPackage(
        HEXAGONAL_LAYERS.DOMAIN.getValue(), HEXAGONAL_LAYERS.APPLICATION.getValue(),
        // Basis dependencies
        "java..", "reactor.core..",
        // Spring dependencies
        "org.springframework.lang..", "org.springframework.data.domain..", "org.springframework.stereotype..",
        // Testing dependencies
        "org.junit..", "reactor.test..", "org.mockito..", "org.assertj.core.api..", "org.springframework.test..",
        // AOT
        "org.springframework.aot.generate..", "org.springframework.beans.factory..");
}
