Feature: Consult sort products

  Scenario: Get products - ok - sorts with sales weight
    Given some metrics weights and page config
      | salesUnits | stock | profitMargin | daysInStock | page | size |
      | 1.0        | 0.0   | 0.0          | 0.0         | 0    | 10   |
    When  consult products sort and paginated
    Then  receive status
      | status |
      | 200    |
    And   get sorted data
      | index | internalId | salesUnits | stock_S | stock_M | stock_L | profitMargin | daysInStock |
      | 0     | 5          | 650        | 0       | 1       | 0       | 0.17         | 31          |
      | 1     | 1          | 100        | 4       | 9       | 0       | 0.5          | 30          |
      | 2     | 3          | 80         | 20      | 2       | 20      | 0.01         | 1           |
      | 3     | 2          | 50         | 35      | 9       | 9       | 0.3          | 150         |
      | 4     | 6          | 20         | 9       | 2       | 5       | 0.39         | 167         |
      | 5     | 4          | 3          | 25      | 30      | 10      | 0.12         | 65          |

  Scenario: Get products - ok - sorts with stock weight
    Given some metrics weights and page config
      | salesUnits | stock | profitMargin | daysInStock | page | size |
      | 0.0        | 1.0   | 0.0          | 0.0         | 0    | 10   |
    When  consult products sort and paginated
    Then  receive status
      | status |
      | 200    |
    And   get sorted data
      | index | internalId | salesUnits | stock_S | stock_M | stock_L | profitMargin | daysInStock |
      | 0     | 4          | 3          | 25      | 30      | 10      | 0.12         | 65          |
      | 1     | 2          | 50         | 35      | 9       | 9       | 0.3          | 150         |
      | 2     | 3          | 80         | 20      | 2       | 20      | 0.01         | 1           |
      | 3     | 6          | 20         | 9       | 2       | 5       | 0.39         | 167         |
      | 4     | 1          | 100        | 4       | 9       | 0       | 0.5          | 30          |
      | 5     | 5          | 650        | 0       | 1       | 0       | 0.17         | 31          |

  Scenario: Get products - ok - sorts with profit weight
    Given some metrics weights and page config
      | salesUnits | stock | profitMargin | daysInStock | page | size |
      | 0.0        | 0.0   | 1.0          | 0.0         | 0    | 10   |
    When  consult products sort and paginated
    Then  receive status
      | status |
      | 200    |
    And   get sorted data
      | index | internalId | salesUnits | stock_S | stock_M | stock_L | profitMargin | daysInStock |
      | 0     | 1          | 100        | 4       | 9       | 0       | 0.5          | 30          |
      | 1     | 6          | 20         | 9       | 2       | 5       | 0.39         | 167         |
      | 2     | 2          | 50         | 35      | 9       | 9       | 0.3          | 150         |
      | 3     | 5          | 650        | 0       | 1       | 0       | 0.17         | 31          |
      | 4     | 4          | 3          | 25      | 30      | 10      | 0.12         | 65          |
      | 5     | 3          | 80         | 20      | 2       | 20      | 0.01         | 1           |

  Scenario: Get products - ok - sorts with days weight
    Given some metrics weights and page config
      | salesUnits | stock | profitMargin | daysInStock | page | size |
      | 0.0        | 0.0   | 0.0          | 1.0         | 0    | 10   |
    When  consult products sort and paginated
    Then  receive status
      | status |
      | 200    |
    And   get sorted data
      | index | internalId | salesUnits | stock_S | stock_M | stock_L | profitMargin | daysInStock |
      | 0     | 6          | 20         | 9       | 2       | 5       | 0.39         | 167         |
      | 1     | 2          | 50         | 35      | 9       | 9       | 0.3          | 150         |
      | 2     | 4          | 3          | 25      | 30      | 10      | 0.12         | 65          |
      | 3     | 5          | 650        | 0       | 1       | 0       | 0.17         | 31          |
      | 4     | 1          | 100        | 4       | 9       | 0       | 0.5          | 30          |
      | 5     | 3          | 80         | 20      | 2       | 20      | 0.01         | 1           |

  Scenario: Get products - ok - sorts with equal weights
    Given some metrics weights and page config
      | salesUnits | stock | profitMargin | daysInStock | page | size |
      | 0.5        | 0.5   | 0.5          | 0.5         | 0    | 10   |
    When  consult products sort and paginated
    Then  receive status
      | status |
      | 200    |
    And   get sorted data
      | index | internalId | salesUnits | stock_S | stock_M | stock_L | profitMargin | daysInStock |
      | 0     | 5          | 650        | 0       | 1       | 0       | 0.17         | 31          |
      | 5     | 4          | 3          | 25      | 30      | 10      | 0.12         | 65          |

  Scenario: Get products - ok - pagination out
    Given some metrics weights and page config
      | salesUnits | stock | profitMargin | daysInStock | page | size |
      | 0          | 0     | 0            | 0           | 10   | 1    |
    When  consult products sort and paginated
    Then  receive status
      | status |
      | 200    |
    And   empty body

  Scenario: Get products - ko - bad parameters
    Given some metrics weights and page config
      | salesUnits | stock | profitMargin | daysInStock | page | size |
      | N          | N     | N            | N           | 0    | 1    |
    When  consult products sort and paginated
    Then  receive status
      | status |
      | 417    |
    And   no body
