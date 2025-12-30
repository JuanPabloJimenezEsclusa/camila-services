package com.camila.api.product.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("[UT][ProductSortCriteria] Product Sort Criteria Unit Tests")
class ProductSortCriteriaUnitTest {

  private static Stream<Arguments> validRequestParamsWithWeights() {
    return Stream.of(
      arguments(Map.of("salesUnits", "0.5", "page", "0", "size", "10"), 0, 10, 4),
      arguments(Map.of("stock", "0.8", "page", "1", "size", "20"), 1, 20, 4),
      arguments(Map.of("profitMargin", "0.3", "daysInStock", "0.7", "page", "2", "size", "15"), 2, 15, 4),
      arguments(Map.of("salesUnits", "1.0", "stock", "0.5", "profitMargin", "0.25", "daysInStock", "0.1"), 0, 10, 4)
    );
  }

  private static Stream<Arguments> validRequestParamsWithoutWeights() {
    return Stream.of(
      arguments(Map.of("page", "0", "size", "10"), 0, 10),
      arguments(Map.of("page", "5", "size", "50"), 5, 50),
      arguments(Map.<String, String>of(), 0, 10)
    );
  }

  private static Stream<Arguments> validWeightExtractionParams() {
    return Stream.of(
      arguments(Map.of("salesUnits", "0.5"), Metrics.SALES_UNITS, 0.5),
      arguments(Map.of("stock", "0.8"), Metrics.STOCK, 0.8),
      arguments(Map.of("profitMargin", "0.3"), Metrics.PROFIT_MARGIN, 0.3),
      arguments(Map.of("daysInStock", "0.7"), Metrics.DAYS_IN_STOCK, 0.7),
      arguments(Map.of("salesUnits", "0.0"), Metrics.SALES_UNITS, 0.0)
    );
  }

  private static Stream<Arguments> invalidPageParams() {
    return Stream.of(
      arguments(Map.of("page", "-1", "size", "10"), "Page number cannot be negative"),
      arguments(Map.of("page", "-5", "size", "20"), "Page number cannot be negative"),
      arguments(Map.of("page", "invalid", "size", "10"), "Invalid page parameter: invalid")
    );
  }

  private static Stream<Arguments> invalidSizeParams() {
    return Stream.of(
      arguments(Map.of("page", "0", "size", "0"), "Page size must be greater than zero"),
      arguments(Map.of("page", "0", "size", "-1"), "Page size must be greater than zero"),
      arguments(Map.of("page", "0", "size", "invalid"), "Invalid size parameter: invalid")
    );
  }

  private static Stream<Arguments> invalidWeightParams() {
    return Stream.of(
      arguments(Map.of("salesUnits", "-0.5"), "Weight for salesUnits must be non-negative"),
      arguments(Map.of("stock", "-1.0"), "Weight for stock must be non-negative"),
      arguments(Map.of("profitMargin", "invalid"), "Invalid weight value for profitMargin"),
      arguments(Map.of("daysInStock", "abc"), "Invalid weight value for daysInStock")
    );
  }

  private static Stream<Arguments> offsetCalculationParams() {
    return Stream.of(
      arguments(0, 10, 0L),
      arguments(1, 10, 10L),
      arguments(2, 20, 40L),
      arguments(5, 15, 75L),
      arguments(10, 50, 500L)
    );
  }

  private static Stream<Arguments> limitCalculationParams() {
    return Stream.of(
      arguments(0, 10, 10L),
      arguments(1, 20, 20L),
      arguments(2, 15, 15L),
      arguments(5, 50, 50L)
    );
  }

  @ParameterizedTest
  @MethodSource("validRequestParamsWithWeights")
  @DisplayName("Should create criteria from valid request params with weights")
  void shouldCreateCriteriaFromValidRequestParamsWithWeights(final Map<String, String> params, final int expectedPage, final int expectedSize, final int expectedWeightsSize) {
    // Given
    // When
    final var criteria = ProductSortCriteria.fromRequestParams(params);

    // Then
    assertThat(criteria)
      .isNotNull()
      .extracting(ProductSortCriteria::page, ProductSortCriteria::size)
      .containsExactly(expectedPage, expectedSize);
    assertThat(criteria.metricWeights()).hasSize(expectedWeightsSize);
  }

  @ParameterizedTest
  @MethodSource("validRequestParamsWithoutWeights")
  @DisplayName("Should create criteria with default weights when no weights provided")
  void shouldCreateCriteriaWithDefaultWeightsWhenNoWeightsProvided(final Map<String, String> params, final int expectedPage, final int expectedSize) {
    // Given
    // When
    final var criteria = ProductSortCriteria.fromRequestParams(params);

    // Then
    assertThat(criteria)
      .isNotNull()
      .extracting(ProductSortCriteria::page, ProductSortCriteria::size)
      .containsExactly(expectedPage, expectedSize);
    assertThat(criteria.metricWeights())
      .hasSize(4)
      .allMatch(mw -> mw.weight() == 1.0);
  }

  @ParameterizedTest
  @MethodSource("validWeightExtractionParams")
  @DisplayName("Should extract metric weights correctly")
  void shouldExtractMetricWeightsCorrectly(final Map<String, String> params, final Metrics metric, final double expectedWeight) {
    // Given
    // When
    final var criteria = ProductSortCriteria.fromRequestParams(params);

    // Then
    final var metricWeight = criteria.metricWeights().stream()
      .filter(mw -> mw.metric() == metric)
      .findFirst()
      .orElseThrow();
    assertThat(metricWeight.weight()).isEqualTo(expectedWeight);
  }

  @ParameterizedTest
  @MethodSource("invalidPageParams")
  @DisplayName("Should throw exception for invalid page parameter")
  void shouldThrowExceptionForInvalidPageParameter(final Map<String, String> params, final String expectedMessage) {
    // When & Then
    assertThatThrownBy(() -> ProductSortCriteria.fromRequestParams(params))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage(expectedMessage);
  }

  @ParameterizedTest
  @MethodSource("invalidSizeParams")
  @DisplayName("Should throw exception for invalid size parameter")
  void shouldThrowExceptionForInvalidSizeParameter(final Map<String, String> params, final String expectedMessage) {
    // When & Then
    assertThatThrownBy(() -> ProductSortCriteria.fromRequestParams(params))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage(expectedMessage);
  }

  @ParameterizedTest
  @MethodSource("invalidWeightParams")
  @DisplayName("Should throw exception for invalid weight parameter")
  void shouldThrowExceptionForInvalidWeightParameter(final Map<String, String> params, final String expectedMessage) {
    // When & Then
    assertThatThrownBy(() -> ProductSortCriteria.fromRequestParams(params))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage(expectedMessage);
  }

  @ParameterizedTest
  @MethodSource("offsetCalculationParams")
  @DisplayName("Should calculate offset correctly")
  void shouldCalculateOffsetCorrectly(final int page, final int size, final long expectedOffset) {
    // Given
    final var criteria = new ProductSortCriteria(List.of(), page, size);

    // When
    final var offset = criteria.offset();

    // Then
    assertThat(offset).isEqualTo(expectedOffset);
  }

  @ParameterizedTest
  @MethodSource("limitCalculationParams")
  @DisplayName("Should return limit correctly")
  void shouldReturnLimitCorrectly(final int page, final int size, final long expectedLimit) {
    // Given
    final var criteria = new ProductSortCriteria(List.of(), page, size);

    // When
    final var limit = criteria.limit();

    // Then
    assertThat(limit).isEqualTo(expectedLimit);
  }

  @Test
  @DisplayName("Should use default page and size when not provided")
  void shouldUseDefaultPageAndSizeWhenNotProvided() {
    // Given
    final var params = Map.<String, String>of();

    // When
    final var criteria = ProductSortCriteria.fromRequestParams(params);

    // Then
    assertThat(criteria)
      .extracting(ProductSortCriteria::page, ProductSortCriteria::size)
      .containsExactly(0, 10);
  }

  @Test
  @DisplayName("Should set non-provided weights to zero when some weights are provided")
  void shouldSetNonProvidedWeightsToZeroWhenSomeWeightsAreProvided() {
    // Given
    final var params = Map.of("salesUnits", "1.0");

    // When
    final var criteria = ProductSortCriteria.fromRequestParams(params);

    // Then
    assertThat(criteria.metricWeights().stream().filter(mw -> mw.metric() == Metrics.SALES_UNITS).findFirst().orElseThrow().weight()).isEqualTo(1.0);
    assertThat(criteria.metricWeights().stream().filter(mw -> mw.metric() == Metrics.STOCK).findFirst().orElseThrow().weight()).isZero();
    assertThat(criteria.metricWeights().stream().filter(mw -> mw.metric() == Metrics.PROFIT_MARGIN).findFirst().orElseThrow().weight()).isZero();
    assertThat(criteria.metricWeights().stream().filter(mw -> mw.metric() == Metrics.DAYS_IN_STOCK).findFirst().orElseThrow().weight()).isZero();
  }
}

