package com.camila.api.product.infrastructure.adapter.output.mongo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("[UT][ProductSorterHelper] Product Sorter Helper Unit Test")
class ProductSorterHelperUnitTest {

  @Test
  @DisplayName("Constructor should throw AssertionError")
  void constructorShouldThrowAssertionError() {
    // Given: Access to private constructor
    // When: Instantiating ProductSorterHelper
    // Then: Should throw AssertionError
    Exception exception = assertThrows(InvocationTargetException.class, () -> {
      final Constructor<ProductSorterHelper> constructor = ProductSorterHelper.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
    });

    assertInstanceOf(AssertionError.class, exception.getCause());
    assertThat(exception.getCause().getMessage()).isEqualTo("not allowed!");
  }
}
