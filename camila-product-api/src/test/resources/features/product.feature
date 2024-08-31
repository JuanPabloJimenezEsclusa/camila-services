Feature: Consult sort products

  Scenario: Get products - ok - sorts with stock weight
    Given some metrics weights and page config
      | salesUnits | stock | page | size |
      | 0.001      | 0.999 | 0    | 10   |
    When  consult products sort and paginated
    Then  receive status
      | status |
      | 200    |
    And   get sorted data
      | index | internalId | salesUnits | stock_S | stock_M | stock_L |
      | 0     | 4          | 3          | 25      | 30      | 10      |
      | 1     | 2          | 50         | 35      | 9       | 9       |
      | 2     | 3          | 80         | 20      | 2       | 20      |
      | 3     | 6          | 20         | 9       | 2       | 5       |
      | 4     | 1          | 100        | 4       | 9       | 0       |
      | 5     | 5          | 650        | 0       | 1       | 0       |

  Scenario: Get products - ok - sorts with sales weight
    Given some metrics weights and page config
      | salesUnits | stock | page | size |
      | 0.999      | 0.001 | 0    | 10   |
    When  consult products sort and paginated
    Then  receive status
      | status |
      | 200    |
    And   get sorted data
      | index | internalId | salesUnits | stock_S | stock_M | stock_L |
      | 0     | 5          | 650        | 0       | 1       | 0       |
      | 1     | 1          | 100        | 4       | 9       | 0       |
      | 2     | 3          | 80         | 20      | 2       | 20      |
      | 3     | 2          | 50         | 35      | 9       | 9       |
      | 4     | 6          | 20         | 9       | 2       | 5       |
      | 5     | 4          | 3          | 25      | 30      | 10      |

  Scenario: Get products - ok - sorts with equal weight
    Given some metrics weights and page config
      | salesUnits | stock | page | size |
      | 0.5        | 0.5   | 0    | 10   |
    When  consult products sort and paginated
    Then  receive status
      | status |
      | 200    |
    And   get sorted data
      | index | internalId | salesUnits | stock_S | stock_M | stock_L |
      | 0     | 5          | 650        | 0       | 1       | 0       |
      | 5     | 4          | 3          | 25      | 30      | 10      |

  Scenario: Get products - ok - pagination out
    Given some metrics weights and page config
      | salesUnits | stock | page | size |
      | 0          | 0     | 10   | 1    |
    When  consult products sort and paginated
    Then  receive status
      | status |
      | 200    |
    And   empty body

  Scenario: Get products - ko - bad parameters
    Given some metrics weights and page config
      | salesUnits | stock | page | size |
      | N          | N     | 0    | 1    |
    When  consult products sort and paginated
    Then  receive status
      | status |
      | 417    |
    And   no body
