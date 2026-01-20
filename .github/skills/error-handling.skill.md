# Error Handling Skill

## Rule
Exceptions MUST be domain-specific, meaningful, and handled centrally at adapter boundaries.

## When This Applies
- When business rules are violated
- When resources are not found
- When validation fails
- When external systems fail
- At REST API boundaries (controllers)

## What is Forbidden
- ❌ Throwing generic `RuntimeException` in business logic
- ❌ Catching `Exception` broadly without re-throwing
- ❌ Using exceptions for control flow
- ❌ Returning null instead of throwing domain exceptions
- ❌ Mixing technical exceptions with domain exceptions

## Required Pattern

### Domain Exceptions (Application Layer)
```java
// ✅ CORRECT: Specific domain exception
public class OrderNotFoundException extends RuntimeException {
    private final String orderId;
    
    public OrderNotFoundException(String orderId) {
        super("Order not found: " + orderId);
        this.orderId = orderId;
    }
    
    public String getOrderId() {
        return orderId;
    }
}

// ❌ WRONG: Generic exception in domain logic
public Order findOrder(String id) {
    if (!exists(id)) {
        throw new RuntimeException("Not found"); // Too generic!
    }
}
```

### Centralized Exception Handling (Adapter Layer)
```java
// ✅ CORRECT: Global exception handler in REST adapter
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
        logger.error("Order not found: orderId={}", ex.getOrderId(), ex);
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("ORDER_NOT_FOUND", ex.getMessage()));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidation(IllegalArgumentException ex) {
        logger.warn("Validation failed: {}", ex.getMessage());
        return ResponseEntity
            .badRequest()
            .body(new ErrorResponse("VALIDATION_ERROR", ex.getMessage()));
    }
}

// ❌ WRONG: Handling exceptions in every controller
@PostMapping
public ResponseEntity<?> create(@RequestBody OrderRequest req) {
    try {
        return ResponseEntity.ok(useCase.execute(req));
    } catch (OrderNotFoundException ex) {
        return ResponseEntity.notFound().build(); // Don't catch here!
    }
}
```

### Validation Exceptions
```java
// ✅ CORRECT: Specific validation exception
public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String orderId, OrderStatus currentStatus, OrderStatus requiredStatus) {
        super(String.format("Invalid order state: orderId=%s, current=%s, required=%s",
            orderId, currentStatus, requiredStatus));
    }
}

// ❌ WRONG: IllegalArgumentException with vague message
throw new IllegalArgumentException("Invalid state"); // What state? Which entity?
```

### Exception Wrapping (Outbound Adapters)
```java
// ✅ CORRECT: Wrap technical exceptions
public class OrderRepositoryAdapter implements OrderRepository {
    @Override
    public Order save(Order order) {
        try {
            OrderEntity entity = OrderEntity.fromDomain(order);
            return jpaRepository.save(entity).toDomain();
        } catch (DataAccessException ex) {
            logger.error("Database error saving order: orderId={}", order.getId(), ex);
            throw new OrderPersistenceException("Failed to save order", ex);
        }
    }
}

// ❌ WRONG: Let technical exceptions bubble to domain
public Order save(Order order) {
    return jpaRepository.save(entity).toDomain(); // JPA exception leaks!
}
```

## Exception Hierarchy
- **Domain Exceptions**: Business rule violations (OrderNotFoundException, InvalidOrderStateException)
- **Application Exceptions**: Use case failures (OrderCreationFailedException)
- **Technical Exceptions**: Infrastructure failures (OrderPersistenceException, ExternalServiceException)

## Enforcement
- Never throw `RuntimeException` directly in domain or application layers
- All domain exceptions MUST extend a base domain exception class
- Controllers MUST NOT catch exceptions (use `@RestControllerAdvice`)
- Exception messages MUST include entity identifiers and context
- Original exceptions MUST be preserved as cause when wrapping
