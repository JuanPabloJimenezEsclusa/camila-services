package com.camila.api.product.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import com.camila.api.product.domain.model.AppliedWeights;
import com.camila.api.product.domain.model.MetricWeight;
import com.camila.api.product.domain.model.Metrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("[UT][ProductWeightResolver] Product Weight Resolver Unit Tests")
class ProductWeightResolverUnitTest {

  private static Stream<Arguments> allMetricsProvidedParams() {
    return Stream.of(
      arguments(
        List.of(
          new MetricWeight(Metrics.SALES_UNITS, 0.5),
          new MetricWeight(Metrics.STOCK, 0.3),
          new MetricWeight(Metrics.PROFIT_MARGIN, 0.15),
          new MetricWeight(Metrics.DAYS_IN_STOCK, 0.05)
        ),
        0.5, 0.3, 0.15, 0.05
      ),
      arguments(
        List.of(
          new MetricWeight(Metrics.SALES_UNITS, 1.0),
          new MetricWeight(Metrics.STOCK, 0.0),
          new MetricWeight(Metrics.PROFIT_MARGIN, 0.0),
          new MetricWeight(Metrics.DAYS_IN_STOCK, 0.0)
        ),
        1.0, 0.0, 0.0, 0.0
      ),
      arguments(
        List.of(
          new MetricWeight(Metrics.SALES_UNITS, 0.25),
          new MetricWeight(Metrics.STOCK, 0.25),
          new MetricWeight(Metrics.PROFIT_MARGIN, 0.25),
          new MetricWeight(Metrics.DAYS_IN_STOCK, 0.25)
        ),
        0.25, 0.25, 0.25, 0.25
      )
    );
  }

  private static Stream<Arguments> partialMetricsProvidedParams() {
    return Stream.of(
      arguments(List.of(new MetricWeight(Metrics.SALES_UNITS, 0.8)), Metrics.SALES_UNITS, 0.8),
      arguments(List.of(new MetricWeight(Metrics.STOCK, 0.6)), Metrics.STOCK, 0.6),
      arguments(List.of(new MetricWeight(Metrics.PROFIT_MARGIN, 0.4)), Metrics.PROFIT_MARGIN, 0.4),
      arguments(List.of(new MetricWeight(Metrics.DAYS_IN_STOCK, 0.2)), Metrics.DAYS_IN_STOCK, 0.2)
    );
  }

  private static Stream<Arguments> zeroWeightParams() {
    return Stream.of(
      arguments(List.of(
        new MetricWeight(Metrics.SALES_UNITS, 0.0),
        new MetricWeight(Metrics.STOCK, 0.0),
        new MetricWeight(Metrics.PROFIT_MARGIN, 0.0),
        new MetricWeight(Metrics.DAYS_IN_STOCK, 0.0)
      ))
    );
  }

  private static Stream<Arguments> mixedWeightParams() {
    return Stream.of(
      arguments(
        List.of(
          new MetricWeight(Metrics.SALES_UNITS, 0.7),
          new MetricWeight(Metrics.PROFIT_MARGIN, 0.3)
        ),
        new AppliedWeights(0.7, ProductWeightResolver.DEFAULT_WEIGHT, 0.3, ProductWeightResolver.DEFAULT_WEIGHT)
      ),
      arguments(
        List.of(
          new MetricWeight(Metrics.STOCK, 0.5),
          new MetricWeight(Metrics.DAYS_IN_STOCK, 0.5)
        ),
        new AppliedWeights(ProductWeightResolver.DEFAULT_WEIGHT, 0.5, ProductWeightResolver.DEFAULT_WEIGHT, 0.5)
      )
    );
  }

  @ParameterizedTest
  @MethodSource("allMetricsProvidedParams")
  @DisplayName("Should resolve weights when all metrics are provided")
  void shouldResolveWeightsWhenAllMetricsAreProvided(final List<MetricWeight> weights, final double salesUnits, final double stock, final double profitMargin, final double daysInStock) {
    // Given
    // When
    final var appliedWeights = ProductWeightResolver.resolve(weights);

    // Then
    assertThat(appliedWeights)
      .isNotNull()
      .extracting(
        AppliedWeights::salesUnitsWeight,
        AppliedWeights::stockWeight,
        AppliedWeights::profitMarginWeight,
        AppliedWeights::daysInStockWeight
      )
      .containsExactly(salesUnits, stock, profitMargin, daysInStock);
  }

  @ParameterizedTest
  @MethodSource("partialMetricsProvidedParams")
  @DisplayName("Should use default weight for missing metrics")
  void shouldUseDefaultWeightForMissingMetrics(final List<MetricWeight> weights, final Metrics providedMetric, final double providedWeight) {
    // Given
    // When
    final var appliedWeights = ProductWeightResolver.resolve(weights);

    // Then
    assertThat(appliedWeights).isNotNull();
    switch (providedMetric) {
      case SALES_UNITS -> {
        assertThat(appliedWeights.salesUnitsWeight()).isEqualTo(providedWeight);
        assertThat(appliedWeights.stockWeight()).isEqualTo(ProductWeightResolver.DEFAULT_WEIGHT);
        assertThat(appliedWeights.profitMarginWeight()).isEqualTo(ProductWeightResolver.DEFAULT_WEIGHT);
        assertThat(appliedWeights.daysInStockWeight()).isEqualTo(ProductWeightResolver.DEFAULT_WEIGHT);
      }
      case STOCK -> {
        assertThat(appliedWeights.salesUnitsWeight()).isEqualTo(ProductWeightResolver.DEFAULT_WEIGHT);
        assertThat(appliedWeights.stockWeight()).isEqualTo(providedWeight);
        assertThat(appliedWeights.profitMarginWeight()).isEqualTo(ProductWeightResolver.DEFAULT_WEIGHT);
        assertThat(appliedWeights.daysInStockWeight()).isEqualTo(ProductWeightResolver.DEFAULT_WEIGHT);
      }
      case PROFIT_MARGIN -> {
        assertThat(appliedWeights.salesUnitsWeight()).isEqualTo(ProductWeightResolver.DEFAULT_WEIGHT);
        assertThat(appliedWeights.stockWeight()).isEqualTo(ProductWeightResolver.DEFAULT_WEIGHT);
        assertThat(appliedWeights.profitMarginWeight()).isEqualTo(providedWeight);
        assertThat(appliedWeights.daysInStockWeight()).isEqualTo(ProductWeightResolver.DEFAULT_WEIGHT);
      }
      case DAYS_IN_STOCK -> {
        assertThat(appliedWeights.salesUnitsWeight()).isEqualTo(ProductWeightResolver.DEFAULT_WEIGHT);
        assertThat(appliedWeights.stockWeight()).isEqualTo(ProductWeightResolver.DEFAULT_WEIGHT);
        assertThat(appliedWeights.profitMarginWeight()).isEqualTo(ProductWeightResolver.DEFAULT_WEIGHT);
        assertThat(appliedWeights.daysInStockWeight()).isEqualTo(providedWeight);
      }
      default -> throw new IllegalStateException("Unexpected metric: " + providedMetric);
    }
  }

  @Test
  @DisplayName("Should use default weight for all metrics when empty list provided")
  void shouldUseDefaultWeightForAllMetricsWhenEmptyListProvided() {
    // Given
    final var weights = List.<MetricWeight>of();

    // When
    final var appliedWeights = ProductWeightResolver.resolve(weights);

    // Then
    assertThat(appliedWeights)
      .isNotNull()
      .extracting(
        AppliedWeights::salesUnitsWeight,
        AppliedWeights::stockWeight,
        AppliedWeights::profitMarginWeight,
        AppliedWeights::daysInStockWeight
      )
      .containsExactly(
        ProductWeightResolver.DEFAULT_WEIGHT,
        ProductWeightResolver.DEFAULT_WEIGHT,
        ProductWeightResolver.DEFAULT_WEIGHT,
        ProductWeightResolver.DEFAULT_WEIGHT
      );
  }

  @Test
  @DisplayName("Should handle duplicate metrics and keep first value")
  void shouldHandleDuplicateMetricsAndKeepFirstValue() {
    // Given
    final var weights = List.of(
      new MetricWeight(Metrics.SALES_UNITS, 0.5),
      new MetricWeight(Metrics.SALES_UNITS, 0.8)
    );

    // When
    final var appliedWeights = ProductWeightResolver.resolve(weights);

    // Then
    assertThat(appliedWeights)
      .isNotNull()
      .extracting(AppliedWeights::salesUnitsWeight)
      .isEqualTo(0.5);
  }

  @Test
  @DisplayName("Should ignore unknown metrics")
  void shouldIgnoreUnknownMetrics() {
    // Given
    final var weights = List.of(
      new MetricWeight(Metrics.UNKNOWN, 0.5),
      new MetricWeight(Metrics.SALES_UNITS, 0.7)
    );

    // When
    final var appliedWeights = ProductWeightResolver.resolve(weights);

    // Then
    assertThat(appliedWeights).isNotNull();
    assertThat(appliedWeights.salesUnitsWeight()).isEqualTo(0.7);
    assertThat(appliedWeights.stockWeight()).isEqualTo(ProductWeightResolver.DEFAULT_WEIGHT);
  }

  @ParameterizedTest
  @MethodSource("zeroWeightParams")
  @DisplayName("Should handle zero weights correctly")
  void shouldHandleZeroWeightsCorrectly(final List<MetricWeight> weights) {
    // Given
    // When
    final var appliedWeights = ProductWeightResolver.resolve(weights);

    // Then
    assertThat(appliedWeights)
      .isNotNull()
      .extracting(
        AppliedWeights::salesUnitsWeight,
        AppliedWeights::stockWeight,
        AppliedWeights::profitMarginWeight,
        AppliedWeights::daysInStockWeight
      )
      .containsExactly(0.0, 0.0, 0.0, 0.0);
  }

  @ParameterizedTest
  @MethodSource("mixedWeightParams")
  @DisplayName("Should handle mixed weight scenarios")
  void shouldHandleMixedWeightScenarios(final List<MetricWeight> weights, final AppliedWeights expected) {
    // Given
    // When
    final var appliedWeights = ProductWeightResolver.resolve(weights);

    // Then
    assertThat(appliedWeights)
      .isNotNull()
      .extracting(
        AppliedWeights::salesUnitsWeight,
        AppliedWeights::stockWeight,
        AppliedWeights::profitMarginWeight,
        AppliedWeights::daysInStockWeight
      )
      .containsExactly(
        expected.salesUnitsWeight(),
        expected.stockWeight(),
        expected.profitMarginWeight(),
        expected.daysInStockWeight()
      );
  }
}
