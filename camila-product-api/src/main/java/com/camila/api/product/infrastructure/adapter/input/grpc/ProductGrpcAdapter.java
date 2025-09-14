package com.camila.api.product.infrastructure.adapter.input.grpc;

import java.time.Duration;

import com.camila.api.product.domain.usecase.ProductUseCase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.scheduler.Schedulers;

/**
 * The type Product grpc adapter.
 */
@GrpcService
class ProductGrpcAdapter extends ProductServiceGrpc.ProductServiceImplBase {
  private static final long TIMEOUT_IN_SECONDS = 30L;
  private final ProductUseCase productUseCase;

  /**
   * Instantiates a new Product grpc adapter.
   *
   * @param productUseCase the product user case
   */
  ProductGrpcAdapter(final ProductUseCase productUseCase) {
    this.productUseCase = productUseCase;
  }

  private static Product convertToGrpcProduct(final com.camila.api.product.domain.model.Product product) {
    return Product.newBuilder()
      .setId(product.id())
      .setInternalId(product.internalId())
      .setName(product.name())
      .setCategory(product.category())
      .setSalesUnits(product.salesUnits())
      .putAllStock(product.stock())
      .setProfitMargin(product.profitMargin())
      .setDaysInStock(product.daysInStock())
      .build();
  }

  @Override
  public void getProductByInternalId(final ProductInternalId request,
                                     final StreamObserver<Product> responseObserver) {
    this.productUseCase.findByInternalId(request.getInternalId())
      .subscribeOn(Schedulers.boundedElastic())
      .timeout(Duration.ofSeconds(TIMEOUT_IN_SECONDS))
      .subscribe(
        product -> responseObserver.onNext(convertToGrpcProduct(product)),
        responseObserver::onError,
        responseObserver::onCompleted);
  }

  @Override
  public void sortByMetricsWeights(final SortByMetricsWeightsRequest request,
                                   final StreamObserver<Product> responseObserver) {
    this.productUseCase.sortByMetricsWeights(request.getRequestParamsMap())
      .subscribeOn(Schedulers.boundedElastic())
      .timeout(Duration.ofSeconds(TIMEOUT_IN_SECONDS))
      .subscribe(
        product -> responseObserver.onNext(convertToGrpcProduct(product)),
        responseObserver::onError,
        responseObserver::onCompleted);
  }
}
