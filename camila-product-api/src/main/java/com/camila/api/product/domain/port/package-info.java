/**
 * Ports belong to the domain layer for several fundamental reasons:
 * 1. Domain-driven contracts - The domain defines what it needs from the outside world
 *    through these interfaces, expressing requirements in domain language.
 * 2. Dependency inversion - Having ports in the domain ensures that dependencies point inward;
 *    adapters (implementations) depend on the domain, not viceversa.
 * 3. Domain isolation - Ports create a protective boundary around domain logic,
 *    shielding it from external technical concerns.
 * 4. Technology agnosticism - Domain-owned ports allow the domain to remain independent of
 *    specific technologies used in adapters.
 * 5. Clean testing - With ports in the domain, you can easily mock external dependencies when testing domain logic.
 * 6. Stable API - Domain ports change only when domain requirements change, not when infrastructure changes.
 */
@NullMarked
package com.camila.api.product.domain.port;

import org.jspecify.annotations.NullMarked;
