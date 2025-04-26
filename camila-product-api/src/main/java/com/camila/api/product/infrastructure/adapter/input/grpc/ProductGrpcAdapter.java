package com.camila.api.product.infrastructure.adapter.input.grpc;

import java.time.Duration;
import java.util.Map;

import com.camila.api.product.domain.usecase.ProductUseCase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.scheduler.Schedulers;

/**
 * The type Product grpc adapter.
 */
@GrpcService
class ProductGrpcAdapter extends ProductServiceGrpc.ProductServiceImplBase {
  private final ProductUseCase productUseCase;

  /**
   * Instantiates a new Product grpc adapter.
   *
   * @param productUseCase the product user case
   */
  ProductGrpcAdapter(final ProductUseCase productUseCase) {
    this.productUseCase = productUseCase;
  }

  @Override
  public void getProductByInternalId(final ProductInternalId request,
                                     final StreamObserver<Product> responseObserver) {
    var domainProduct = productUseCase.findByInternalId(request.getInternalId());
    domainProduct
      .subscribeOn(Schedulers.boundedElastic())
      .timeout(Duration.ofMillis(5_000L))
      .subscribe(
        product -> responseObserver.onNext(convertToGrpcProduct(product)),
        responseObserver::onError,
        responseObserver::onCompleted);
  }

  @Override
  public void sortByMetricsWeights(final SortByMetricsWeightsRequest request,
                                   final StreamObserver<Product> responseObserver) {
    Map<String, String> requestParams = request.getRequestParamsMap();
    var domainProducts = productUseCase.sortByMetricsWeights(requestParams);
    domainProducts
      .subscribeOn(Schedulers.boundedElastic())
      .timeout(Duration.ofMillis(30_000L))
      .subscribe(
        product -> responseObserver.onNext(convertToGrpcProduct(product)),
        responseObserver::onError,
        responseObserver::onCompleted);
  }

  private Product convertToGrpcProduct(final com.camila.api.product.domain.model.Product product) {
    return Product.newBuilder()
      .setId(product.id())
      .setInternalId(product.internalId())
      .setName(product.name())
      .setCategory(product.category())
      .setSalesUnits(product.salesUnits())
      .putAllStock(product.stock())
      .build();
  }
}
