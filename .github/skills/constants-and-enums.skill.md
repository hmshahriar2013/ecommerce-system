# Constants and Enums Skill

## Rule
All finite value sets MUST use enums. All repeated values MUST be named constants.

## When This Applies
- When a value can only be one of a fixed set (status, type, category)
- When a numeric or string literal appears more than once
- When a value has business meaning (thresholds, limits, codes)
- When values represent configuration that shouldn't change at runtime

## What is Forbidden
- ❌ String literals representing states, types, or categories
- ❌ Magic numbers (thresholds, limits, offsets)
- ❌ Duplicated constant values across classes
- ❌ Using strings where enums would be type-safe

## Required Pattern

### Use Enums for Finite Value Sets
```java
// ✅ CORRECT: Enum for order status
public enum OrderStatus {
    DRAFT,
    SUBMITTED,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

public class Order {
    private OrderStatus status;
    
    public void submit() {
        if (status != OrderStatus.DRAFT) {
            throw new InvalidOrderStateException("Only draft orders can be submitted");
        }
        this.status = OrderStatus.SUBMITTED;
    }
}

// ❌ WRONG: String literals for status
public class Order {
    private String status;
    
    public void submit() {
        if (!"DRAFT".equals(status)) { // Typo-prone, not type-safe!
            throw new InvalidOrderStateException("Only draft orders can be submitted");
        }
        this.status = "SUBMITTED";
    }
}
```

### Named Constants for Business Values
```java
// ✅ CORRECT: Named constants in domain
public class Order {
    private static final int MAX_ITEMS_PER_ORDER = 100;
    private static final int MIN_ITEMS_PER_ORDER = 1;
    private static final BigDecimal MINIMUM_ORDER_VALUE = new BigDecimal("10.00");
    
    public void addItem(OrderItem item) {
        if (items.size() >= MAX_ITEMS_PER_ORDER) {
            throw new OrderLimitExceededException(MAX_ITEMS_PER_ORDER);
        }
        items.add(item);
    }
}

// ❌ WRONG: Magic numbers
public void addItem(OrderItem item) {
    if (items.size() >= 100) { // What is 100? Why 100?
        throw new OrderLimitExceededException(100);
    }
    items.add(item);
}
```

### Constants Class for Shared Values
```java
// ✅ CORRECT: Constants class for application-wide values
public final class ApplicationConstants {
    private ApplicationConstants() {} // Prevent instantiation
    
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_FIELD = "createdAt";
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
}

// Usage
int pageSize = ApplicationConstants.DEFAULT_PAGE_SIZE;

// ❌ WRONG: Scattered magic values
int pageSize = 20; // Repeated in multiple places
```

### Enum Location by Domain Meaning
```java
// ✅ CORRECT: Domain enum in domain layer
// domain/OrderStatus.java
public enum OrderStatus {
    DRAFT, SUBMITTED, CONFIRMED
}

// ✅ CORRECT: Application-level enum in application layer
// application/SortDirection.java
public enum SortDirection {
    ASC, DESC
}

// ❌ WRONG: Domain enum in adapter layer
// adapters/inbound/OrderStatus.java - NO! Belongs in domain!
```

### Enums with Behavior
```java
// ✅ CORRECT: Enum with domain behavior
public enum PaymentMethod {
    CREDIT_CARD {
        @Override
        public boolean requiresVerification() {
            return true;
        }
    },
    BANK_TRANSFER {
        @Override
        public boolean requiresVerification() {
            return false;
        }
    },
    CASH {
        @Override
        public boolean requiresVerification() {
            return false;
        }
    };
    
    public abstract boolean requiresVerification();
}

// ❌ WRONG: If-else chains on strings
public boolean requiresVerification(String paymentMethod) {
    if ("CREDIT_CARD".equals(paymentMethod)) return true;
    if ("BANK_TRANSFER".equals(paymentMethod)) return false;
    // ... brittle and error-prone
}
```

### Configuration Constants (Infrastructure Layer)
```java
// ✅ CORRECT: Configuration constants from properties
@Configuration
public class ApplicationConfiguration {
    public static final String PROPERTY_PREFIX = "app";
    
    @Value("${app.max-retry-count:3}")
    private int maxRetryCount;
    
    @Value("${app.timeout-seconds:30}")
    private int timeoutSeconds;
}

// ❌ WRONG: Hardcoded configuration
public class SomeService {
    private static final int MAX_RETRIES = 3; // Should be configurable!
}
```

## Enforcement
- Any value appearing 2+ times MUST be a named constant
- Any finite value set MUST be an enum
- Enums MUST be in domain or application layer based on business meaning
- Constants MUST have descriptive names (not `CONSTANT_1`, `VALUE_A`)
- Magic numbers and strings are NEVER acceptable in production code
