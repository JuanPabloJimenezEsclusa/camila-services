package com.camila.api.product.infrastructure.adapter.input;


import com.camila.api.product.application.usecase.DefaultProductUseCase;
import com.camila.api.product.domain.port.ProductRepository;
import com.camila.api.product.domain.usecase.ProductUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for use case beans in the hexagonal architecture.
 *
 * <p>This class serves as a bridge between the Spring framework and the application layer.
 * By placing Spring's DI annotations (@Configuration, @Bean) here in the adapter layer,
 * we keep the application layer (DefaultProductUseCase) free from framework dependencies.</p>
 *
 * This approach:
 * - Maintains a clean separation between layers
 * - Avoids contaminating the application layer with Spring annotations
 * - Ensures the architecture is properly layered according to hexagonal principles
 * - Makes the application layer more testable and framework-agnostic
 * - Allows the architecture tests to pass by keeping Spring dependencies in framework layers only
 */
@Configuration
class UseCaseConfig {
  /**
   * Product use case product use case.
   *
   * @param productRepository the product repository
   * @return the product use case
   */
  @Bean
  public ProductUseCase productUseCase(final ProductRepository productRepository) {
    return new DefaultProductUseCase(productRepository);
  }
}
