package com.camila.api.product.infrastructure.adapter.output.cache;

import com.camila.api.product.domain.model.AppliedWeights;
import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.port.ProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Output-layer decorator that adds caching to the ProductRepository implementation.
 * <p>
 * Placing caching at the output port level keeps the application and input adapters
 * free from framework-specific annotations and ensures caching is close to the
 * data source (repository) as a cross-cutting concern of the infrastructure layer.
 */
@EnableCaching
public class CachedProductRepositoryDecorator implements ProductRepository {

  private final ProductRepository delegate;

  /**
   * Instantiates a new Cached product repository decorator.
   *
   * @param delegate the delegate
   */
  public CachedProductRepositoryDecorator(final ProductRepository delegate) {
    this.delegate = delegate;
  }

  @Cacheable(cacheNames = "findByInternalId", key = "#internalId")
  @Override
  public Mono<Product> findByInternalId(final String internalId) {
    return delegate.findByInternalId(internalId);
  }

  @Cacheable(cacheNames = "sortedProducts", key = """
    {
      #appliedWeights.salesUnitsWeight,
      #appliedWeights.stockWeight,
      #appliedWeights.profitMarginWeight,
      #appliedWeights.daysInStockWeight,
      #offset,
      #limit
    }
    """)
  @Override
  public Flux<Product> sortByMetricsWeights(final AppliedWeights appliedWeights, final long offset, final long limit) {
    return delegate.sortByMetricsWeights(appliedWeights, offset, limit);
  }
}
