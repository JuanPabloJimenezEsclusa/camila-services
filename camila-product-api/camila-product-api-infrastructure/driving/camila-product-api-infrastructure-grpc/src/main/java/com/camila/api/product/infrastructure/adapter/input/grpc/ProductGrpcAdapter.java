package com.camila.api.product.infrastructure.adapter.input.grpc;

import java.time.Duration;

import com.camila.api.product.domain.usecase.ProductUseCase;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;
import reactor.core.scheduler.Schedulers;

/**
 * The type Product grpc adapter.
 */
@GrpcService
class ProductGrpcAdapter extends ProductServiceGrpc.ProductServiceImplBase {
  private static final long TIMEOUT_IN_SECONDS = 30L;

  private final ProductUseCase productUseCase;
  private final ProductGrpcMapper productGrpcMapper;

  /**
   * Instantiates a new Product grpc adapter.
   *
   * @param productUseCase    the product use case
   * @param productGrpcMapper the product dto mapper
   */
  ProductGrpcAdapter(final ProductUseCase productUseCase, final ProductGrpcMapper productGrpcMapper) {
    this.productUseCase = productUseCase;
    this.productGrpcMapper = productGrpcMapper;
  }

  @Override
  public void getProductByInternalId(final ProductInternalId request,
                                     final StreamObserver<Product> responseObserver) {
    this.productUseCase.findByInternalId(request.getInternalId())
      .subscribeOn(Schedulers.boundedElastic())
      .timeout(Duration.ofSeconds(TIMEOUT_IN_SECONDS))
      .subscribe(
        product -> responseObserver.onNext(productGrpcMapper.toProduct(product)),
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
        product -> responseObserver.onNext(productGrpcMapper.toProduct(product)),
        responseObserver::onError,
        responseObserver::onCompleted);
  }
}
