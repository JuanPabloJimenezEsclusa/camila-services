package com.camila.api.product.domain.model;

import java.util.stream.Stream;

public enum Metrics {
  SALES_UNITS("salesUnits"),
  STOCK("stock"),
  PROFIT_MARGIN("profitMargin"),
  DAYS_IN_STOCK("daysInStock"),
  UNKNOWN("unknown");

  private final String description;

  Metrics(final String description) {
    this.description = description;
  }

  public static Metrics getMetrics(final String description) {
    return Stream.of(values())
      .filter(metrics -> metrics.description.equalsIgnoreCase(description))
      .findFirst()
      .orElse(UNKNOWN);
  }

  public String getDescription() {
    return this.description;
  }
}
