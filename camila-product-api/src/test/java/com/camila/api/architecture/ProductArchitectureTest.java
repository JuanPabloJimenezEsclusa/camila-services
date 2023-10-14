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
    INFRASTRUCTURE(BASE_PKG + ".infrastructure.."),
    PRESENTATION(BASE_PKG + ".presentation..");

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
    .layer(HEXAGONAL_LAYERS.INFRASTRUCTURE.name()).definedBy(HEXAGONAL_LAYERS.INFRASTRUCTURE.getValue())
    .layer(HEXAGONAL_LAYERS.PRESENTATION.name()).definedBy(HEXAGONAL_LAYERS.PRESENTATION.getValue())

    // external hexagonal layers. No others layer access
    .whereLayer(HEXAGONAL_LAYERS.INFRASTRUCTURE.name()).mayNotBeAccessedByAnyLayer()
    .whereLayer(HEXAGONAL_LAYERS.PRESENTATION.name()).mayNotBeAccessedByAnyLayer()

    // application layer. Accesible only by externals layers
    .whereLayer(HEXAGONAL_LAYERS.APPLICATION.name()).mayOnlyBeAccessedByLayers(
      HEXAGONAL_LAYERS.INFRASTRUCTURE.name(), HEXAGONAL_LAYERS.PRESENTATION.name())
    .whereLayer(HEXAGONAL_LAYERS.APPLICATION.name()).mayOnlyAccessLayers(HEXAGONAL_LAYERS.DOMAIN.name())

    // domain layer. Not use other layer class
    .whereLayer(HEXAGONAL_LAYERS.DOMAIN.name()).mayOnlyBeAccessedByLayers(
      HEXAGONAL_LAYERS.INFRASTRUCTURE.name(), HEXAGONAL_LAYERS.PRESENTATION.name(), HEXAGONAL_LAYERS.APPLICATION.name())
    .whereLayer(HEXAGONAL_LAYERS.DOMAIN.name()).mayNotAccessAnyLayer()
    .ensureAllClassesAreContainedInArchitecture();

  @ArchTest
  static final ArchRule DOMAIN_ONLY_DEPEND_ON_STANDARD =
    classes().that().resideInAPackage(HEXAGONAL_LAYERS.DOMAIN.getValue())
      .should().onlyDependOnClassesThat().resideInAnyPackage(HEXAGONAL_LAYERS.DOMAIN.getValue(),
        "java..", "reactor.core..");
}
