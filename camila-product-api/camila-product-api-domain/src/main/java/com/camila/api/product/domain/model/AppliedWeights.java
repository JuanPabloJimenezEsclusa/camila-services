package com.camila.api.product.domain.model;

public record AppliedWeights(
  double salesUnitsWeight,
  double stockWeight,
  double profitMarginWeight,
  double daysInStockWeight
) {
}
