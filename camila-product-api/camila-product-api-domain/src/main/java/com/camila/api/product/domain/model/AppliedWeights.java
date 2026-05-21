package com.camila.api.product.domain.model;

/**
 * The type Applied weights.
 */
public record AppliedWeights(
  double salesUnitsWeight,
  double stockWeight,
  double profitMarginWeight,
  double daysInStockWeight
) {
}
