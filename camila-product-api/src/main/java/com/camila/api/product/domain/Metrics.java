package com.camila.api.product.domain;

import java.util.stream.Stream;

/**
 * The enum Metrics.
 */
public enum Metrics {
  SALES_UNITS("salesUnits"),
  STOCK("stock"),
  UNKNOWN("unknown");

  private final String description;

  /**
   * Gets metrics.
   *
   * @param description the description
   * @return the metrics
   */
  public static Metrics getMetrics(String description) {
    return Stream.of(Metrics.values())
      .filter(metrics -> metrics.description.equalsIgnoreCase(description))
      .findFirst()
      .orElse(UNKNOWN);
  }

  Metrics(String description) {
    this.description = description;
  }

  /**
   * Gets description.
   *
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }
}
