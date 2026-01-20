# No Magic Values Skill

## Rule
All literals with business meaning MUST be named constants or configuration properties.

## When This Applies
- Numeric thresholds, limits, or boundaries
- String codes, types, or identifiers
- Time durations and intervals
- Collection sizes and capacities
- Any value that has semantic meaning in the business domain

## What is Forbidden
- ❌ Numeric literals in business logic (except 0, 1, -1 in obvious contexts)
- ❌ String literals that represent business concepts
- ❌ Hardcoded configuration values
- ❌ Repeated literal values across methods or classes
- ❌ Unexplained magic numbers in calculations

## Required Pattern

### Business Constants
```java
// ✅ CORRECT: Named constants with clear meaning
public class Order {
    private static final int MAX_LINE_ITEMS = 50;
    private static final int MIN_LINE_ITEMS = 1;
    private static final BigDecimal MAX_ORDER_AMOUNT = new BigDecimal("10000.00");
    private static final int PROCESSING_DELAY_HOURS = 24;
    
    public void addItem(OrderItem item) {
        if (items.size() >= MAX_LINE_ITEMS) {
            throw new OrderLimitExceededException(
                "Cannot exceed " + MAX_LINE_ITEMS + " items per order"
            );
        }
        items.add(item);
    }
}

// ❌ WRONG: Magic numbers
public void addItem(OrderItem item) {
    if (items.size() >= 50) { // What is 50? Why 50?
        throw new OrderLimitExceededException("Too many items");
    }
    items.add(item);
}
```

### Status Codes and Types
```java
// ✅ CORRECT: Named constants for codes
public final class OrderStatusCodes {
    private OrderStatusCodes() {}
    
    public static final String PENDING = "PENDING";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String SHIPPED = "SHIPPED";
}

// Better: Use enum instead
public enum OrderStatus {
    PENDING, CONFIRMED, SHIPPED
}

// ❌ WRONG: String literals scattered everywhere
if ("PENDING".equals(order.getStatus())) { // Repeated literal
    // ...
}
```

### Time and Duration Constants
```java
// ✅ CORRECT: Named duration constants
public class OrderService {
    private static final Duration ORDER_TIMEOUT = Duration.ofMinutes(5);
    private static final int RETRY_ATTEMPTS = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);
    
    public Order processOrder(String orderId) {
        return Retry.decorateSupplier(
            Retry.of("orderService", 
                RetryConfig.custom()
                    .maxAttempts(RETRY_ATTEMPTS)
                    .waitDuration(RETRY_DELAY)
                    .build()
            ),
            () -> orderRepository.findById(orderId)
        ).get();
    }
}

// ❌ WRONG: Magic time values
Thread.sleep(5000); // 5000 what? Why 5000?
```

### Externalized Configuration
```java
// ✅ CORRECT: Configuration from properties
@Configuration
@ConfigurationProperties(prefix = "order")
public class OrderConfiguration {
    private int maxItems = 50;
    private BigDecimal maxAmount = new BigDecimal("10000.00");
    private int processingDelayHours = 24;
    
    // Getters and setters
}

// application.properties
order.max-items=50
order.max-amount=10000.00
order.processing-delay-hours=24

// ❌ WRONG: Hardcoded in service
public class OrderService {
    public void process() {
        if (items.size() > 50) { // Should be configurable!
            // ...
        }
    }
}
```

### Calculation Constants
```java
// ✅ CORRECT: Named constants for calculations
public class DiscountCalculator {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.21");
    private static final BigDecimal BULK_DISCOUNT_THRESHOLD = new BigDecimal("1000.00");
    private static final BigDecimal BULK_DISCOUNT_RATE = new BigDecimal("0.10");
    private static final int BULK_QUANTITY_THRESHOLD = 10;
    
    public BigDecimal calculateTotal(BigDecimal subtotal, int quantity) {
        BigDecimal discount = BigDecimal.ZERO;
        
        if (quantity >= BULK_QUANTITY_THRESHOLD || subtotal.compareTo(BULK_DISCOUNT_THRESHOLD) >= 0) {
            discount = subtotal.multiply(BULK_DISCOUNT_RATE);
        }
        
        BigDecimal afterDiscount = subtotal.subtract(discount);
        BigDecimal tax = afterDiscount.multiply(TAX_RATE);
        
        return afterDiscount.add(tax);
    }
}

// ❌ WRONG: Magic numbers in calculation
public BigDecimal calculateTotal(BigDecimal subtotal, int quantity) {
    BigDecimal discount = subtotal.multiply(new BigDecimal("0.10")); // What is 0.10?
    return subtotal.subtract(discount).multiply(new BigDecimal("1.21")); // What is 1.21?
}
```

### Collection Sizes
```java
// ✅ CORRECT: Named capacity constants
public class OrderRepository {
    private static final int CACHE_INITIAL_CAPACITY = 100;
    private static final float CACHE_LOAD_FACTOR = 0.75f;
    private static final int MAX_CACHE_SIZE = 1000;
    
    private final Map<String, Order> cache = new HashMap<>(
        CACHE_INITIAL_CAPACITY,
        CACHE_LOAD_FACTOR
    );
}

// ❌ WRONG: Magic collection sizes
private final Map<String, Order> cache = new HashMap<>(100, 0.75f); // Why these values?
```

### HTTP Status and Error Codes
```java
// ✅ CORRECT: Named constants for HTTP codes (or use HttpStatus enum)
public final class ApiConstants {
    private ApiConstants() {}
    
    public static final String ERROR_CODE_NOT_FOUND = "E404";
    public static final String ERROR_CODE_INVALID_INPUT = "E400";
    public static final String ERROR_CODE_UNAUTHORIZED = "E401";
}

// Better: Use Spring's HttpStatus enum
return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

// ❌ WRONG: Magic status codes
return ResponseEntity.status(404).body(error); // Use HttpStatus.NOT_FOUND!
```

### Acceptable Literal Usage (Limited Cases)
```java
// ✅ ACCEPTABLE: Obvious mathematical constants
if (quantity < 0) { // 0 is obvious minimum
    throw new IllegalArgumentException("Quantity cannot be negative");
}

if (list.isEmpty() || list.size() == 1) { // 1 is obvious
    return list;
}

// ✅ ACCEPTABLE: Loop indices
for (int i = 0; i < items.size(); i++) { // 0 and standard iteration
    // ...
}

// ❌ NOT ACCEPTABLE: Business logic numbers
if (age > 18) { // 18 is a business rule - should be constant!
    // ...
}
```

### Test Code (Special Case)
```java
// ✅ ACCEPTABLE: Literals in tests with clear context
@Test
void shouldRejectOrderWithTooManyItems() {
    // given
    Order order = new Order();
    for (int i = 0; i < 51; i++) { // 51 exceeds the 50-item limit
        order.addItem(new OrderItem("item-" + i, BigDecimal.ONE));
    }
    
    // when & then
    assertThrows(OrderLimitExceededException.class, () -> order.validate());
}

// ✅ BETTER: Use the actual constant
@Test
void shouldRejectOrderWithTooManyItems() {
    int maxItems = Order.MAX_LINE_ITEMS; // Reference the constant
    
    Order order = new Order();
    for (int i = 0; i < maxItems + 1; i++) {
        order.addItem(new OrderItem("item-" + i, BigDecimal.ONE));
    }
    
    assertThrows(OrderLimitExceededException.class, () -> order.validate());
}
```

## Constant Naming Convention
```java
// Domain constants
public static final int MAX_ITEMS_PER_ORDER = 50;
public static final Duration ORDER_EXPIRY_DURATION = Duration.ofDays(30);

// Configuration keys
public static final String CONFIG_KEY_MAX_RETRIES = "app.max-retries";

// Error codes
public static final String ERROR_CODE_ORDER_NOT_FOUND = "ORD_404";
```

## Enforcement
- Every numeric literal (except 0, 1, -1 in obvious contexts) MUST be a named constant
- Every string literal with business meaning MUST be a constant or enum
- Configuration values MUST come from application.properties
- Constants MUST have descriptive names explaining their purpose
- Repeated values MUST be extracted to constants
- Test code MAY use literals when the value is explained in context
