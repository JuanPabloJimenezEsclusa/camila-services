package com.camila.api.product.domain;

import java.util.Map;

/**
 * The type Product.
 */
public record Product(
  String id,
  String internalId,
  String name,
  String category,
  int salesUnits,
  Map<String, Integer> stock
) { }
