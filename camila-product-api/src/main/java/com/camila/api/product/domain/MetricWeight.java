package com.camila.api.product.domain;

/**
 * The type Metric weight.
 */
public record MetricWeight (
  Metrics metric,
  double weight
) { }
