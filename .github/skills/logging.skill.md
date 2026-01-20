# Logging Skill

## Rule
All error conditions and exceptions MUST be logged with structured logging and appropriate context.

## When This Applies
- When catching any exception
- When detecting an error condition (validation failure, business rule violation)
- When calling external systems or databases
- At use case boundaries (application layer)
- When state transitions fail

## What is Forbidden
- ❌ Catching exceptions without logging them
- ❌ String concatenation in log messages
- ❌ Logging without context (request ID, entity ID, operation name)
- ❌ Using `System.out.println()` or `printStackTrace()`
- ❌ Swallowing exceptions silently

## Required Pattern

### Use SLF4J with Placeholders
```java
// ✅ CORRECT: Structured logging with context
private static final Logger logger = LoggerFactory.getLogger(CreateOrderUseCase.class);

public Order execute(String orderId, String userId) {
    logger.debug("Executing CreateOrder use case: orderId={}, userId={}", orderId, userId);
    try {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        return order;
    } catch (OrderNotFoundException ex) {
        logger.error("Order not found: orderId={}, userId={}", orderId, userId, ex);
        throw ex;
    }
}

// ❌ WRONG: No logging on exception
public Order execute(String orderId) {
    return orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId)); // Silent failure!
}

// ❌ WRONG: String concatenation
logger.error("Order not found: " + orderId); // Don't concatenate!
```

### Context Must Be Included
```java
// ✅ CORRECT: Log with entity context
logger.error("Failed to activate sample: sampleId={}, status={}", 
    sample.getId(), sample.getStatus(), exception);

// ❌ WRONG: Generic message without context
logger.error("Activation failed", exception);
```

### Exception Swallowing is Forbidden
```java
// ✅ CORRECT: Log before re-throwing or wrapping
try {
    externalService.call();
} catch (IOException ex) {
    logger.error("External service call failed: serviceUrl={}", serviceUrl, ex);
    throw new ExternalServiceException("Service unavailable", ex);
}

// ❌ WRONG: Silent catch
try {
    externalService.call();
} catch (IOException ex) {
    // Ignored - NO! Must log!
}
```

## Enforcement
- Every `catch` block MUST contain a log statement
- Every thrown domain exception MUST be logged at its origin
- Log levels: ERROR for exceptions, WARN for recoverable issues, DEBUG for flow tracking
- Include relevant business context in every log statement
