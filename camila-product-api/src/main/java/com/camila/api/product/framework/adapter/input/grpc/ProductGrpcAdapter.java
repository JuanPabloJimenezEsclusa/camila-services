package com.camila.api.product.framework.adapter.input.grpc;

import com.camila.api.product.application.usercase.ProductUserCase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;

/**
 * The type Product grpc adapter.
 */
@GrpcService
class ProductGrpcAdapter extends ProductServiceGrpc.ProductServiceImplBase {
  private final ProductUserCase productUserCase;

  /**
   * Instantiates a new Product grpc adapter.
   *
   * @param productUserCase the product user case
   */
  ProductGrpcAdapter(ProductUserCase productUserCase) {
    this.productUserCase = productUserCase;
  }

  @Override
  public void getProductByInternalId(ProductInternalId request,
                                     StreamObserver<Product> responseObserver) {
    var domainProduct = productUserCase.findByInternalId(request.getInternalId());
    domainProduct
      .subscribeOn(Schedulers.boundedElastic())
      .timeout(Duration.ofMillis(5_000L))
      .subscribe(
        product -> responseObserver.onNext(convertToGrpcProduct(product)),
        responseObserver::onError,
        responseObserver::onCompleted);
  }

  @Override
  public void sortByMetricsWeights(SortByMetricsWeightsRequest request,
                                   StreamObserver<Product> responseObserver) {
    Map<String, String> requestParams = request.getRequestParamsMap();
    var domainProducts = productUserCase.sortByMetricsWeights(requestParams);
    domainProducts
      .subscribeOn(Schedulers.boundedElastic())
      .timeout(Duration.ofMillis(30_000L))
      .subscribe(
        product -> responseObserver.onNext(convertToGrpcProduct(product)),
        responseObserver::onError,
        responseObserver::onCompleted);
  }

  private Product convertToGrpcProduct(com.camila.api.product.domain.Product product) {
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
