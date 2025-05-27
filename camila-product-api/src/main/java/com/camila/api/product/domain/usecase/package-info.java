/**
 * Use cases belong to the domain layer for several fundamental reasons:
 * 1. Business logic orchestration - Use cases coordinate domain entities and services
 *    to fulfill specific business operations.
 * 2. Primary ports representation - Use cases serve as primary (driving)
 *    ports that define how external components interact with your domain.
 * 3. Technology independence - Interface contains no implementation details,
 *    keeping it free from infrastructure concerns.
 * 4. Dependency control - By placing use cases in the domain, you ensure that the domain controls
 *    how it's used rather than adapting to external needs.
 * 5. Business process encapsulation - Use cases represent complete business processes
 *    defined using domain vocabulary.
 * 6. Single responsibility - Each use case addresses a specific business capability.
 * 7. Testing isolation - Domain use cases can be tested independently of infrastructure concerns.
 * 8. Stable API boundary - Use cases provide a stable interface that changes only
 *    when business requirements change, not when external systems change.
 */
@NullMarked
package com.camila.api.product.domain.usecase;

import org.jspecify.annotations.NullMarked;
