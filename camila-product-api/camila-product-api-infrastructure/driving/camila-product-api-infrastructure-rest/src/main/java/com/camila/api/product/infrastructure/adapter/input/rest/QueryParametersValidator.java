package com.camila.api.product.infrastructure.adapter.input.rest;

import java.util.Map;
import java.util.regex.Pattern;

import reactor.core.publisher.Mono;

public final class QueryParametersValidator {
  private static final Pattern PATTERN = Pattern.compile("^\\d+(?:\\.\\d*)?$");

  private QueryParametersValidator() {
  }

  public static Mono<Map<String, String>> validate(final Map<String, String> params) {
    return Mono.just(params)
      .filter(map -> map.entrySet().stream()
        .allMatch(entry -> isValidValue(entry.getValue())))
      .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid parameters")));
  }

  private static boolean isValidValue(final String value) {
    return value.length() <= 7 && PATTERN.matcher(value).matches();
  }
}
