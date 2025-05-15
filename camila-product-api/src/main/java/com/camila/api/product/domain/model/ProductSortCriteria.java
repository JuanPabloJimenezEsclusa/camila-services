package com.camila.api.product.domain.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the criteria for sorting products.
 */
public class ProductSortCriteria {
  private static final String DEFAULT_PAGE_NUMBER = "0";
  private static final String DEFAULT_PAGE_SIZE = "10";

  private final List<MetricWeight> metricWeights;
  private final int page;
  private final int size;

  private ProductSortCriteria(final List<MetricWeight> metricWeights, final int page, final int size) {
    this.metricWeights = metricWeights;
    this.page = page;
    this.size = size;
  }

  /**
   * Factory method to create a ProductSortCriteria instance from request parameters.
   *
   * @param requestParams Map of request parameters
   * @return A validated ProductSortCriteria instance
   * @throws IllegalArgumentException if page or size parameters are invalid
   */
  public static ProductSortCriteria fromRequestParams(final Map<String, String> requestParams) {
    final List<MetricWeight> weights = extractMetricWeights(requestParams);
    final int page = parseIntParam(requestParams.getOrDefault("page", DEFAULT_PAGE_NUMBER), "page");
    final int size = parseIntParam(requestParams.getOrDefault("size", DEFAULT_PAGE_SIZE), "size");

    if (page < 0) {
      throw new IllegalArgumentException("Page number cannot be negative");
    }

    if (size <= 0) {
      throw new IllegalArgumentException("Page size must be greater than zero");
    }

    return new ProductSortCriteria(weights, page, size);
  }

  private static List<MetricWeight> extractMetricWeights(final Map<String, String> requestParams) {
    final Map<Metrics, Double> providedWeights = requestParams.entrySet().stream()
      .filter(param -> Metrics.getMetrics(param.getKey()) != Metrics.UNKNOWN)
      .collect(HashMap::new, (map, entry) -> {
        final var metric = Metrics.getMetrics(entry.getKey());
        try {
          final var weight = Double.parseDouble(entry.getValue());
          if (weight < 0) {
            throw new IllegalArgumentException("Weight for %s must be non-negative".formatted(entry.getKey()));
          }
          map.put(metric, weight);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Invalid weight value for %s".formatted(entry.getKey()));
        }
      }, HashMap::putAll);

    final double defaultWeight = !providedWeights.isEmpty() ? 0.0 : 1.0;

    return Arrays.stream(Metrics.values())
      .filter(metric -> metric != Metrics.UNKNOWN)
      .map(metric -> new MetricWeight(metric, providedWeights.getOrDefault(metric, defaultWeight)))
      .toList();
  }

  private static int parseIntParam(final String value, final String paramName) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid %s parameter: %s".formatted(paramName, value));
    }
  }

  /**
   * Gets the list of metric weights.
   *
   * @return A list of MetricWeight objects
   */
  public List<MetricWeight> getMetricWeights() {
    return metricWeights;
  }

  /**
   * Calculates the offset for pagination based on the page number and size.
   *
   * @return The offset as a long value
   */
  public long getOffset() {
    return (long) page * size;
  }

  /**
   * Gets the limit (page size) for pagination.
   *
   * @return The page size as a long value
   */
  public long getLimit() {
    return size;
  }
}
