@CustomerSegments @Pricing
Feature:Pricing can be displayed based on customer segments
    As a RelOS client
    I want to offer personalized shopping experiences for customers in my customer segment
    so these segments of customer can have the shopping experiences they desire

  Scenario Outline: Customer segment pricing is displayed when customer segment condition is matched
    Given I login to the registered user with email ID <CUSTOMER_SEGMENT_ID>
    When I add item <ITEM_NAME> to the cart
    Then the item <ITEM_NAME> list and purchase price is $9999.00

    Examples:
      | CUSTOMER_SEGMENT_ID           | ITEM_NAME                           |
      | itestsegment@elasticpath.com  | productUsedInCustomerSegmentsItests |

  @HeaderAuth
  Scenario: Customer segment pricing is not displayed when customer segment condition is matched
    Given I login as a registered user
    When I add item productUsedInCustomerSegmentsItests to the cart
    Then the item productUsedInCustomerSegmentsItests list and purchase price is $15.00

  @HeaderAuth
  Scenario Outline: Customer segment pricing is displayed when header override includes customer segment trait
    Given I login as a registered user
    And I submit request header with the user traits <TRAITS_VALUE>
    When I add item <ITEM_NAME> to the cart
    Then the item <ITEM_NAME> list and purchase price is $9999.00

    Examples:
      | TRAITS_VALUE                   | ITEM_NAME                           |
      | CUSTOMER_SEGMENT=ITEST_SEGMENT | productUsedInCustomerSegmentsItests |