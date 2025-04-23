package com.camila.api.product.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * The type Product.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Product(
  String id,
  String internalId,
  String name,
  String category,
  int salesUnits,
  Map<String, Integer> stock
) {}
