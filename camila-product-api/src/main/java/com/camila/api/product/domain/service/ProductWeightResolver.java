package com.camila.api.product.domain.service;

import java.util.List;
import java.util.stream.Collectors;

import com.camila.api.product.domain.model.AppliedWeights;
import com.camila.api.product.domain.model.MetricWeight;
import com.camila.api.product.domain.model.Metrics;

/**
 * The type Product weight resolver.
 */
public class ProductWeightResolver {

  public static final double DEFAULT_WEIGHT = 0.0000000001;

  private ProductWeightResolver() {
  }

  /**
   * Resolves a list of {@link MetricWeight} objects into an {@link AppliedWeights} object.
   * This method processes the input list of metric weights, maps them to their corresponding
   * metrics, and applies default weights for any missing metrics. The resulting
   * {@link AppliedWeights} object contains the resolved weights for the specified metrics.
   *
   * @param metricsWeights A list of {@link MetricWeight} objects representing the weights for various metrics.
   * @return An {@link AppliedWeights} object containing the resolved weights for the specified metrics.
   */
  public static AppliedWeights resolve(final List<MetricWeight> metricsWeights) {
    final var weightsMap = metricsWeights.stream()
      .collect(Collectors.toMap(
        MetricWeight::metric,
        MetricWeight::weight,
        (existing, replacement) -> existing)
      );

    return new AppliedWeights(
      weightsMap.getOrDefault(Metrics.SALES_UNITS, DEFAULT_WEIGHT),
      weightsMap.getOrDefault(Metrics.STOCK, DEFAULT_WEIGHT),
      weightsMap.getOrDefault(Metrics.PROFIT_MARGIN, DEFAULT_WEIGHT),
      weightsMap.getOrDefault(Metrics.DAYS_IN_STOCK, DEFAULT_WEIGHT)
    );
  }
}
