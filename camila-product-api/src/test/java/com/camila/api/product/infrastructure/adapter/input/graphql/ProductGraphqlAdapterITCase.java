package com.camila.api.product.infrastructure.adapter.input.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import com.camila.api.product.application.usecase.DefaultProductUseCase;
import com.camila.api.product.domain.model.Product;
import com.camila.api.product.infrastructure.adapter.input.graphql.config.GraphqlConfig;
import com.camila.api.product.infrastructure.adapter.input.security.LocalSecurityConfig;
import com.camila.api.product.infrastructure.adapter.output.mongo.ProductMongoAdapter;
import com.camila.api.product.infrastructure.adapter.output.mongo.ProductMongoMapperImpl;
import com.camila.api.product.infrastructure.adapter.output.mongo.config.DataTestConfig;
import com.camila.api.product.infrastructure.adapter.output.mongo.config.LocalConfig;
import de.flapdoodle.embed.mongo.runtime.Mongod;
import de.flapdoodle.embed.mongo.spring.autoconfigure.ReactiveClientServerFactory;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientHealthAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.cloud.client.loadbalancer.LoadBalancerDefaultMappingsProviderAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;

@GraphQlTest(
  properties = "repository.technology=mongo",
  controllers = ProductGraphqlAdapter.class
)
/*
  Slicing spring configuration.
  Benefits:
  - Faster test execution: Only loads necessary components
  - Improved test isolation: Focus on specific layers/components
  - Greater control: Clear definition of test boundaries
  - Reduced complexity: Tests only load what they need
*/
@ImportAutoConfiguration(exclude = {
  // gRPC
  GrpcClientAutoConfiguration.class,
  GrpcClientHealthAutoConfiguration.class,
  GrpcServerFactoryAutoConfiguration.class,
  LoadBalancerDefaultMappingsProviderAutoConfiguration.class
})
@Import({
  // Slicing spring configuration
  // Framework adapter input layer
  GraphqlConfig.class,
  ProductGraphqlAdapter.class,
  // Security
  LocalSecurityConfig.class,
  // Application layer
  DefaultProductUseCase.class,
  // Framework adapter output layer
  ProductMongoAdapter.class,
  ProductMongoMapperImpl.class,
  LocalConfig.class,
  DataTestConfig.class,
  Mongod.class,
  ReactiveClientServerFactory.class
})
@AutoConfigureGraphQlTester
@DisplayName("[IT][ProductGraphqlAdapter] Product graphql adapter test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductGraphqlAdapterITCase {
  private static final String ERROR_EXPECTED_VALUE = "Variable 'salesUnits' has an invalid value: "
    + "Expected a Number input, but it was a 'String'";

  @Autowired
  private GraphQlTester graphQlTester;

  @Test
  @DisplayName("[ProductGraphqlAdapter] findByInternalId ok")
  @Order(2)
  void findById() {
    final var query = """
      query findById($internalId: ID) {
        findById(internalId: $internalId) {
          id, internalId, category, name, salesUnits, stock
        }
      }
      """;

    graphQlTester.document(query)
      .variable("internalId", "1")
      .execute()
      .path("data.findById.internalId").entity(String.class)
      .satisfies(internalId -> assertThat(internalId).contains("1"));
  }

  @Test
  @DisplayName("[ProductGraphqlAdapter] sort products with stock more weight")
  @Order(2)
  void sortProductsWithStockMoreWeight() {
    final var query = """
      query sortProducts($salesUnits: Float, $stock: Float, $withDetails: Boolean!) {
        sortProducts(salesUnits: $salesUnits, stock: $stock) {
          id @include(if: $withDetails)
          internalId @include(if: $withDetails)
          category @include(if: $withDetails)
          name
          salesUnits
          stock
        }
      }
      """;

    graphQlTester.document(query)
      .variable("salesUnits", 0.001)
      .variable("stock", 0.999)
      .variable("withDetails", false)
      .execute()
      .path("data.sortProducts[0].salesUnits").entity(Integer.class).isEqualTo(3)
      .path("data.sortProducts[0].stock.S").entity(Integer.class).isEqualTo(25)
      .path("data.sortProducts[0].stock.M").entity(Integer.class).isEqualTo(30)
      .path("data.sortProducts[0].stock.L").entity(Integer.class).isEqualTo(10)
      .path("data.sortProducts[0].stock.XL").pathDoesNotExist()
      .path("data.sortProducts").entityList(Product.class)
      .satisfies(products -> assertThat(products).hasSize(6));
  }

  @Test
  @DisplayName("[ProductGraphqlAdapter] sortProducts with page filter")
  @Order(2)
  void sortProductsWithPageFilter() {
    final var query = """
      query sortProducts($salesUnits: Float, $stock: Float, $page: Int, $size: Int!) {
        sortProducts(salesUnits: $salesUnits, stock: $stock, page: $page, size: $size) {
          name
          salesUnits
          stock
        }
      }
      """;

    graphQlTester.document(query)
      .variable("salesUnits", 0.001)
      .variable("stock", 0.999)
      .variable("page", 0)
      .variable("size", 2)
      .execute()
      .path("data.sortProducts[0].name").entity(String.class).isEqualTo("PLEATED T-SHIRT")
      .path("data.sortProducts").entityList(Product.class)
      .satisfies(products -> assertThat(products).hasSize(2));
  }

  @Test
  @DisplayName("[ProductGraphqlAdapter] sortProducts with page out")
  @Order(2)
  void sortProductsWithPageOut() {
    final var query = """
      query sortProducts($salesUnits: Float, $stock: Float, $page: Int, $size: Int) {
        sortProducts(salesUnits: $salesUnits, stock: $stock, page: $page, size: $size) { id }
      }
      """;

    graphQlTester.document(query)
      .variable("salesUnits", 0.001)
      .variable("stock", 0.999)
      .variable("page", 1)
      .variable("size", 10)
      .execute()
      .path("data.sortProducts").entityList(Product.class)
      .satisfies(products -> assertThat(products).isEmpty());
  }

  @Test
  @DisplayName("[ProductGraphqlAdapter] sortProducts with constraint violation")
  @Order(2)
  void sortProductsWithConstraintViolation() {
    final var query = """
      query sortProducts($salesUnits: Float, $stock: Float) {
        sortProducts(salesUnits: $salesUnits, stock: $stock) { id }
      }
      """;

    graphQlTester.document(query)
      .variable("salesUnits", "X")
      .variable("stock", "Y")
      .execute()
      .errors().expect(responseError -> Objects.requireNonNull(responseError.getMessage())
        .contains(ERROR_EXPECTED_VALUE));
  }
}
