package com.camila.api.product.infrastructure.adapter.input.rest;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validates query parameters for product API requests.
 * <p>This validator provides defensive validation that ensures parameter values meet requirements
 * specified in the OpenAPI definition (product.yml), particularly pattern and length constraints.</p>
 *
 * <p>While the OpenAPI-generated ProductsApi interface includes some validation annotations,
 * it doesn't fully implement all constraints defined in the YAML specification. This validator
 * serves as an additional validation layer to ensure consistency with API documentation.</p>
 *
 * <p>Current validation rules:
 * - Values must be numeric and start with at least one digit
 * - Values can have at most one decimal point
 * - Values must be 7 characters or fewer length</p>
 */
@Component
public class QueryParametersValidator {
  private static final Pattern PATTERN = Pattern.compile("^\\d+(?:\\.\\d*)?$");

  /**
   * Validate.
   *
   * @param params the params
   * @return the request map
   */
  public Mono<Map<String, String>> validate(final Map<String, String> params) {
    return Mono.just(params)
      .filter(map -> map.entrySet().stream()
        .allMatch(entry -> isValidValue(entry.getValue())))
      .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid parameters")));
  }

  private boolean isValidValue(final String value) {
    return value.length() <= 7 && PATTERN.matcher(value).matches();
  }
}
