package com.camila.api.product.infrastructure.adapter.input.cache;

import java.util.Map;

import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.usecase.ProductUseCase;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Decorator that adds caching behavior to the {@link ProductUseCase} implementation.
 *
 * <p>This class follows the decorator pattern to add Spring caching functionality while
 * maintaining clean hexagonal architecture. By implementing caching in a decorator rather
 * than directly in the application layer, we:
 *
 * <ul>
 *   <li>Keep Spring framework dependencies (@Cacheable) isolated to the infrastructure layer</li>
 *   <li>Maintain a clean application layer without framework-specific annotations</li>
 *   <li>Follow separation of concerns - business logic in application layer,
 *       cross-cutting concerns like caching in appropriate adapters</li>
 *   <li>Improve testability of the application layer without Spring dependencies</li>
 *   <li>Enable architecture tests to verify proper layering of dependencies</li>
 * </ul>
 *
 * <p>This implementation caches results from both {@code findByInternalId} and
 * {@code sortByMetricsWeights} methods using different cache regions and key strategies
 * appropriate for each method's parameters.</p>
 */
@EnableCaching
public class CachedProductDecorator implements ProductUseCase {
  private final ProductUseCase delegate;

  /**
   * Instantiates a new Cached product decorator.
   *
   * @param delegate the delegate
   */
  public CachedProductDecorator(final ProductUseCase delegate) {
    this.delegate = delegate;
  }

  @Cacheable(cacheNames = "findByInternalId", key = "#internalId")
  @Override
  public Mono<Product> findByInternalId(final String internalId) {
    return delegate.findByInternalId(internalId);
  }

  @Cacheable(cacheNames = "sortedProducts", key = "T(java.util.Objects).hash(#requestParams)")
  @Override
  public Flux<Product> sortByMetricsWeights(final Map<String, String> requestParams) {
    return delegate.sortByMetricsWeights(requestParams);
  }
}
