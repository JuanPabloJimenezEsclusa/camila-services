package com.camila.api.product.domain.service;

import java.util.List;
import java.util.stream.Collectors;

import com.camila.api.product.domain.model.AppliedWeights;
import com.camila.api.product.domain.model.MetricWeight;
import com.camila.api.product.domain.model.Metrics;

public final class ProductWeightResolver {

  public static final double DEFAULT_WEIGHT = 0.0000000001;

  private ProductWeightResolver() {
  }

  public static AppliedWeights resolve(final List<MetricWeight> metricsWeights) {
    final var weightsMap = metricsWeights.stream()
      .collect(Collectors.toMap(
        MetricWeight::metric,
        MetricWeight::weight,
        (existing, _) -> existing)
      );

    return new AppliedWeights(
      weightsMap.getOrDefault(Metrics.SALES_UNITS, DEFAULT_WEIGHT),
      weightsMap.getOrDefault(Metrics.STOCK, DEFAULT_WEIGHT),
      weightsMap.getOrDefault(Metrics.PROFIT_MARGIN, DEFAULT_WEIGHT),
      weightsMap.getOrDefault(Metrics.DAYS_IN_STOCK, DEFAULT_WEIGHT)
    );
  }
}
