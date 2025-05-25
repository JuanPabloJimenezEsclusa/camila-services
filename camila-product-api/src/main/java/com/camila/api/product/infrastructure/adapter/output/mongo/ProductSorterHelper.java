package com.camila.api.product.infrastructure.adapter.output.mongo;

import static com.camila.api.product.domain.service.ProductWeightResolver.DEFAULT_WEIGHT;

import java.util.ArrayList;
import java.util.List;

import com.camila.api.product.domain.model.AppliedWeights;
import com.camila.api.product.domain.model.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.AccumulatorOperators;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.ObjectOperators;
import org.springframework.data.mongodb.core.aggregation.SkipOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.VariableOperators;

/**
 * The type Product sorter helper.
 */
@Slf4j
class ProductSorterHelper {
  private ProductSorterHelper() {
  }

  /**
   * Build limit operation.
   *
   * @param limit the limit
   * @return the limit operation
   */
  static LimitOperation buildLimitOperation(final long limit) {
    return Aggregation.limit(limit);
  }

  /**
   * Build skip operation.
   *
   * @param offset the offset
   * @return the skip operation
   */
  static SkipOperation buildSkipOperation(final long offset) {
    return Aggregation.skip(offset);
  }

  /**
   * Build sort operation.
   *
   * @return the sort operation
   */
  static SortOperation buildSortOperation() {
    return Aggregation.sort(Sort.Direction.DESC, "weightedScore");
  }

  /**
   * Build weighted score field add fields operation.
   *
   * @param appliedWeights the metrics weights
   * @return the add fields operation
   */
  static AddFieldsOperation buildWeightedScoreField(final AppliedWeights appliedWeights) {
    final var weightExpressions = new ArrayList<AggregationExpression>();

    if (appliedWeights.salesUnitsWeight() > DEFAULT_WEIGHT) {
      weightExpressions.add(getWeightBySimpleMultiply(Metrics.SALES_UNITS.getDescription(), appliedWeights.salesUnitsWeight()));
    }
    if (appliedWeights.stockWeight() > DEFAULT_WEIGHT) {
      weightExpressions.add(getStockByWeights(appliedWeights.stockWeight()));
    }
    if (appliedWeights.profitMarginWeight() > DEFAULT_WEIGHT) {
      weightExpressions.add(getWeightBySimpleMultiply(Metrics.PROFIT_MARGIN.getDescription(), appliedWeights.profitMarginWeight()));
    }
    if (appliedWeights.daysInStockWeight() > DEFAULT_WEIGHT) {
      weightExpressions.add(getWeightBySimpleMultiply(Metrics.DAYS_IN_STOCK.getDescription(), appliedWeights.daysInStockWeight()));
    }

    return Aggregation.addFields()
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

  private static AggregationExpression joinWeightExpressions(final List<AggregationExpression> weightExpressions) {
    return weightExpressions.stream()
      .reduce((aggregationExpression, aggregationExpression2) ->
        ArithmeticOperators.valueOf(aggregationExpression).add(aggregationExpression2))
      .orElseGet(() -> getWeightBySimpleMultiply(Metrics.SALES_UNITS.getDescription(), 1D));
  }

  private static AggregationExpression getWeightBySimpleMultiply(final String metricDescription,
                                                                 final double weight) {
    return ArithmeticOperators.Multiply.valueOf(metricDescription).multiplyBy(weight);
  }

  private static AggregationExpression getStockByWeights(final double weight) {
    final AggregationExpression divisor = AccumulatorOperators.Sum.sumOf(
      VariableOperators.Map.itemsOf(ObjectOperators.valueOf(Metrics.STOCK.getDescription()).toArray())
        .as("size")
        .andApply(ConvertOperators.valueOf("$$size.v").convertToDouble()));

    final AggregationExpression dividend = ArrayOperators.Size.lengthOfArray(
      ObjectOperators.valueOf(Metrics.STOCK.getDescription()).toArray());

    final AggregationExpression stockByWeights = ArithmeticOperators.Multiply
      .valueOf(ArithmeticOperators.Divide.valueOf(divisor).divideBy(dividend))
      .multiplyBy(weight);

    log.debug(stockByWeights.toString());
    return stockByWeights;
  }
}
