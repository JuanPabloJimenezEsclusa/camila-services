/**
 * Exceptions belong to the domain layer for several important reasons:
 * 1. Domain vocabulary ownership: Exceptions are part of the domain's ubiquitous language and
 *    represent business rule violations or domain constraints.
 * 2. Independence principle: The domain layer should be self-contained and independent of outer layers.
 *    By defining its own exceptions, it maintains this independence.
 * 3. Contract definition: Domain ports (interfaces) need to declare what exceptions they might
 *    throw as part of their contract with adapters.
 * 4. Technical isolation: Domain exceptions shield domain logic from infrastructure-specific
 *    error details by wrapping technical exceptions into domain-meaningful ones.
 * 5. Consistent error handling: Adapters can uniformly handle domain exceptions regardless
 *    of which use case or domain service threw them.
 * 6. Inversion of dependencies: External layers depend on the domain, not viceversa.
 *    Domain-defined exceptions maintain this control inversion.
 */
@NullMarked
package com.camila.api.product.domain.exception;

import org.jspecify.annotations.NullMarked;
