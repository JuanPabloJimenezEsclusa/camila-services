package com.camila.api.product.infrastructure.adapter.input.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.stream.Stream;

import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.usecase.ProductUseCase;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("[UT][ProductGrpcAdapter] Product gRPC Adapter Unit Tests")
class ProductGrpcAdapterUnitTest {

  @Mock
  private StreamObserver<com.camila.api.product.infrastructure.adapter.input.grpc.Product> responseObserver;

  @Mock
  private ProductUseCase productUseCase;

  @InjectMocks
  private ProductGrpcAdapter productGrpcAdapter;

  @Captor
  private ArgumentCaptor<com.camila.api.product.infrastructure.adapter.input.grpc.Product> productCaptor;

  private static Stream<Arguments> sortProductsParams() {
    // salesUnits, stock, profitMargin, daysInStock, page, size
    return Stream.of(
      Arguments.of(1.0, 1.0, 1.0, 1.0, 0, 10),
      Arguments.of(0.7, 0.3, 0.0, 0.0, 0, 20),
      Arguments.of(0.25, 0.25, 0.50, 0.0, 5, 15),
      Arguments.of(0.25, 0.75, 0.0, 0.0, 2, 25)
    );
  }

  private static Product createDomainProduct(final String internalId, final String name, final String category) {
    final var salesUnits = 10;
    final var stock = Map.of("warehouse1", 10, "warehouse2", 20);
    final var profitMargin = 0.5D;
    final var daysInStock = 45;

    return new Product("id_" + internalId, internalId, name, category,
      salesUnits, stock, profitMargin, daysInStock);
  }

  @Test
  @DisplayName("Should return product by internal ID")
  void shouldReturnProductByInternalId() {
    // Given
    final var internalId = "123";
    final var request = ProductInternalId.newBuilder().setInternalId(internalId).build();
    final var domainProduct = createDomainProduct(internalId, "Test Product", "Category");
    when(productUseCase.findByInternalId(internalId)).thenReturn(Mono.just(domainProduct));

    // When
    productGrpcAdapter.getProductByInternalId(request, responseObserver);

    // Then
    verify(productUseCase).findByInternalId(internalId);
    verify(responseObserver).onNext(productCaptor.capture());
    verify(responseObserver).onCompleted();
    verifyNoMoreInteractions(productUseCase, responseObserver);

    final com.camila.api.product.infrastructure.adapter.input.grpc.Product capturedProduct = productCaptor.getValue();
    assertEquals(internalId, capturedProduct.getInternalId());
    assertEquals("Test Product", capturedProduct.getName());
    assertEquals("Category", capturedProduct.getCategory());
    assertEquals(10.0f, capturedProduct.getSalesUnits());
  }

  @Test
  @DisplayName("Should handle error when fetching product by internal ID")
  void shouldHandleErrorWhenFetchingProductByInternalId() {
    // Given
    final var internalId = "123";
    final var request = ProductInternalId.newBuilder().setInternalId(internalId).build();
    final var exception = new RuntimeException("Product not found");
    when(productUseCase.findByInternalId(internalId)).thenReturn(Mono.error(exception));

    // When
    productGrpcAdapter.getProductByInternalId(request, responseObserver);

    // Then
    verify(productUseCase).findByInternalId(internalId);
    verifyNoMoreInteractions(productUseCase);
  }

  @ParameterizedTest(name = "{index} -> salesUnits={0}, stock={1}, profitMargin={2}, stock={3}, page={4}, size={5}")
  @MethodSource("sortProductsParams")
  @DisplayName("Should sort products with different parameters")
  void shouldSortProductsByMetricsWeights(final Double salesUnits, final Double stock,
                                          final Double profitMargin, final Double daysInStock,
                                          final Integer page, final Integer size) {
    // Given
    final var requestParams = Map.of(
      "salesUnits", salesUnits.toString(),
      "stock", stock.toString(),
      "profitMargin", profitMargin.toString(),
      "daysInStock", daysInStock.toString(),
      "page", page.toString(),
      "size", size.toString()
    );

    final var request = SortByMetricsWeightsRequest.newBuilder().putAllRequestParams(requestParams).build();
    final var product1 = createDomainProduct("1", "Product 1", "Category 1");
    final var product2 = createDomainProduct("2", "Product 2", "Category 2");

    when(productUseCase.sortByMetricsWeights(requestParams)).thenReturn(Flux.just(product1, product2));

    // When
    productGrpcAdapter.sortByMetricsWeights(request, responseObserver);

    // Then
    verify(productUseCase).sortByMetricsWeights(requestParams);
    verifyNoMoreInteractions(productUseCase);
  }

  @Test
  @DisplayName("Should handle empty results when sorting products")
  void shouldHandleEmptyResultsWhenSortingProducts() {
    // Given
    final var requestParams = Map.of(
      "salesUnits", "0.6",
      "stock", "0.4",
      "profitMargin", "0.0",
      "daysInStock", "0.0",
      "page", "0",
      "size", "10"
    );

    final var request = SortByMetricsWeightsRequest.newBuilder()
      .putAllRequestParams(requestParams)
      .build();

    when(productUseCase.sortByMetricsWeights(requestParams)).thenReturn(Flux.empty());

    // When
    productGrpcAdapter.sortByMetricsWeights(request, responseObserver);

    // Then
    verify(productUseCase).sortByMetricsWeights(requestParams);
    verify(responseObserver, times(0)).onNext(any());
    verify(responseObserver).onCompleted();
    verifyNoMoreInteractions(productUseCase, responseObserver);
  }

  @Test
  @DisplayName("Should handle error when sorting products")
  void shouldHandleErrorWhenSortingProducts() {
    // Given
    final var requestParams = Map.of(
      "salesUnits", "0.6",
      "stock", "0.4",
      "profitMargin", "0.0",
      "daysInStock", "0.0",
      "page", "0",
      "size", "10"
    );

    final var request = SortByMetricsWeightsRequest.newBuilder()
      .putAllRequestParams(requestParams)
      .build();

    final var exception = new RuntimeException("Database error");
    when(productUseCase.sortByMetricsWeights(requestParams)).thenReturn(Flux.error(exception));

    // When
    productGrpcAdapter.sortByMetricsWeights(request, responseObserver);

    // Then
    verify(productUseCase).sortByMetricsWeights(requestParams);
    verifyNoMoreInteractions(productUseCase);
  }
}
