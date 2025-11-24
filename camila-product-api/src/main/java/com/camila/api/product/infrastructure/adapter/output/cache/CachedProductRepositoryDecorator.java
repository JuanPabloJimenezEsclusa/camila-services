package com.camila.api.product.infrastructure.adapter.output.cache;

import com.camila.api.product.domain.model.AppliedWeights;
import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.port.CachePort;
import com.camila.api.product.domain.port.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Output-layer decorator that adds reactive caching to the ProductRepository implementation.
 *
 * <p>Placing caching at the output port level keeps the application and input adapters
 * free from framework-specific annotations and ensures caching is close to the
 * data source (repository) as a cross-cutting concern of the infrastructure layer.
 */
@Slf4j
@Component
@EnableCaching
public class CachedProductRepositoryDecorator implements ProductRepository {

  private static final String FIND_BY_INTERNAL_ID_CACHE = "findByInternalId";
  private static final String SORTED_PRODUCTS_CACHE = "sortedProducts";

  private final ProductRepository delegate;
  private final CachePort cacheService;

  private static String makeSortedProductsKey(final AppliedWeights weights, final long offset, final long limit) {
    return "su:%s:st:%s:pm:%s:ds:%s:off:%d:lim:%d".formatted(
      weights.salesUnitsWeight(),
      weights.stockWeight(),
      weights.profitMarginWeight(),
      weights.daysInStockWeight(),
      offset, limit);
  }

  /**
   * Instantiates a new Cached product repository decorator.
   *
   * @param delegate the delegate
   * @param cacheService reactive cache service
   */
  public CachedProductRepositoryDecorator(final ProductRepository delegate, final CachePort cacheService) {
    this.delegate = delegate;
    this.cacheService = cacheService;
  }

  @Override
  public Mono<Product> findByInternalId(final String internalId) {
    log.debug("CachedProductRepositoryDecorator.findByInternalId called with ID: {}", internalId);

    return this.cacheService.get(FIND_BY_INTERNAL_ID_CACHE, internalId, Product.class)
      .doOnNext(_ -> log.debug("Cache HIT for product with internalId: {}", internalId))
      .switchIfEmpty(
        Mono.defer(() -> {
          log.debug("Cache MISS for product with internalId: {}", internalId);
          return delegate.findByInternalId(internalId)
            .doOnNext(product -> log.debug("Found product in database: {}", product.internalId()))
            .flatMap(product ->
              this.cacheService.put(FIND_BY_INTERNAL_ID_CACHE, internalId, product)
                .thenReturn(product)
                .doOnNext(p -> log.debug("Cached product with internalId: {}", p.internalId()))
            )
            .doOnError(e -> log.error("Error retrieving product with internalId: {}, error: {}",
                                    internalId, e.getMessage()));
        })
      );
  }

  @Override
  public Flux<Product> sortByMetricsWeights(final AppliedWeights appliedWeights, final long offset, final long limit) {
    final String key = makeSortedProductsKey(appliedWeights, offset, limit);
    log.debug("CachedProductRepositoryDecorator.sortByMetricsWeights called with key: {}", key);

    return this.cacheService.getList(SORTED_PRODUCTS_CACHE, key, Product.class)
      .doOnNext(products -> {
        if (!products.isEmpty() && log.isDebugEnabled()) {
          log.debug("Cache HIT for sortByMetricsWeights with {} products", products.size());
        }
      })
      .flatMapMany(products -> products.isEmpty() ? Flux.empty() : Flux.fromIterable(products))
      .switchIfEmpty(
        Flux.defer(() -> delegate.sortByMetricsWeights(appliedWeights, offset, limit)
          .doOnNext(product -> log.trace("Got product from database sort: {}", product.internalId()))
          .collectList()
          .doOnNext(products -> log.debug("Found {} products in database sort", products.size()))
          .flatMapMany(products ->
            this.cacheService.putList(SORTED_PRODUCTS_CACHE, key, products)
              .thenMany(Flux.fromIterable(products))
              .doOnNext(p -> log.trace("Returning sorted product: {}", p.internalId()))
          ))
      );
  }
}
