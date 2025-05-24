package com.camila.api.product.domain.model;

import java.util.stream.Stream;

/**
 * The enum Metrics.
 */
public enum Metrics {
  SALES_UNITS("salesUnits"),
  STOCK("stock"),
  PROFIT_MARGIN("profitMargin"),
  DAYS_IN_STOCK("daysInStock"),
  UNKNOWN("unknown");

  private final String description;

  /**
   * Constructor for the Metrics enum.
   *
   * @param description the description of the metric
   */
  Metrics(final String description) {
    this.description = description;
  }

  /**
   * Gets the corresponding Metrics enum value based on the provided description.
   *
   * @param description the description of the metric
   * @return the matching Metrics enum value, or UNKNOWN if no match is found
   */
  public static Metrics getMetrics(final String description) {
    return Stream.of(values())
      .filter(metrics -> metrics.description.equalsIgnoreCase(description))
      .findFirst()
      .orElse(UNKNOWN);
  }

  /**
   * Gets the description of the metric.
   *
   * @return the description of the metric
   */
  public String getDescription() {
    return this.description;
  }
}
