/**
 * Model belong to the domain layer for several key reasons:
 * 1. Core business concepts: Models represent the primary business entities and value objects
 *    that encapsulate the essence of your domain.
 * 2. Independence from infrastructure: Domain models must be free from infrastructure concerns
 *    (databases, frameworks) to ensure the domain logic remains pure and technology-agnostic.
 * 3. Ubiquitous language: Models embody the business vocabulary.
 * 4. Business rules encapsulation: Models can contain domain rules.
 * 5. Dependency inversion: External layers depend on domain models, not viceversa,
 *    maintaining the "ports and adapters" pattern.
 * 6. Single source of truth: Having models in the domain establishes a canonical
 *    representation that all layers must respect and adapt to.
 */
@NullMarked
package com.camila.api.product.domain.model;

import org.jspecify.annotations.NullMarked;
