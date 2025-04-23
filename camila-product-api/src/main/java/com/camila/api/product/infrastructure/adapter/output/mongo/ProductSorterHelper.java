package com.camila.api.product.infrastructure.adapter.output.mongo;

import com.camila.api.product.domain.model.MetricWeight;
import com.camila.api.product.domain.model.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.*;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * The type Product sorter helper.
 */
@Slf4j
class ProductSorterHelper {
  private ProductSorterHelper() {
    throw new AssertionError("not allowed!");
  }

  /**
   * Build limit operation.
   *
   * @param limit the limit
   * @return the limit operation
   */
  static LimitOperation buildLimitOperation(final long limit) {
    return limit(limit);
  }

  /**
   * Build skip operation.
   *
   * @param offset the offset
   * @return the skip operation
   */
  static SkipOperation buildSkipOperation(final long offset) {
    return skip(offset);
  }

  /**
   * Build sort operation.
   *
   * @return the sort operation
   */
  static SortOperation buildSortOperation() {
    return sort(Sort.Direction.DESC, "weightedScore");
  }

  /**
   * Build weighted score field add fields operation.
   *
   * @param metricsWeights the metrics weights
   * @return the add fields operation
   */
  static AddFieldsOperation buildWeightedScoreField(final List<MetricWeight> metricsWeights) {
    var weightExpressions = metricsWeights.stream()
      .filter(metricWeight -> metricWeight.metric() != Metrics.UNKNOWN)
      .map(ProductSorterHelper::getAggregationExpression)
      .toList();

    return addFields()
      .addFieldWithValue("weightedScore", joinWeightExpressions(weightExpressions))
      .build();
  }

  /**
   * Build options aggregation options.
   *
   * @return the aggregation options
   */
  static AggregationOptions buildOptions() {
    return AggregationOptions
      .builder()
      .allowDiskUse(true)
      .build();
  }

  private static AggregationExpression getAggregationExpression(final MetricWeight metricWeight) {
    return metricWeight.metric() == Metrics.STOCK
      ? getStockByWeights(metricWeight.weight())
      : getSalesUnitsByWeights(metricWeight.weight());
  }

  private static AggregationExpression joinWeightExpressions(final List<AggregationExpression> weightExpressions) {
    if (weightExpressions.isEmpty()) {
      return getSalesUnitsByWeights(100D);
    }

    if (weightExpressions.size() == 1) {
      return weightExpressions.getFirst();
    }

    return ArithmeticOperators.valueOf(weightExpressions.get(0)).add(weightExpressions.get(1));
  }

  private static AggregationExpression getSalesUnitsByWeights(final double weight) {
    return ArithmeticOperators.Multiply
      .valueOf(Metrics.SALES_UNITS.getDescription())
      .multiplyBy(weight);
  }

  private static AggregationExpression getStockByWeights(final double weight) {
    AggregationExpression divisor = AccumulatorOperators.Sum.sumOf(
      VariableOperators.Map.itemsOf(ObjectOperators.valueOf(Metrics.STOCK.getDescription()).toArray())
        .as("size")
        .andApply(ConvertOperators.valueOf("$$size.v").convertToDouble()));

    AggregationExpression dividend = ArrayOperators.Size.lengthOfArray(
      ObjectOperators.valueOf(Metrics.STOCK.getDescription()).toArray());

    AggregationExpression stockByWeights = ArithmeticOperators.Multiply
      .valueOf(ArithmeticOperators.Divide.valueOf(divisor).divideBy(dividend))
      .multiplyBy(weight);

    log.debug(stockByWeights.toString());
    return stockByWeights;
  }
}
