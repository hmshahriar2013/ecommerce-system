# Localization Skill

## Rule
All user-facing messages MUST come from externalized message bundles, not hardcoded strings.

## When This Applies
- Error messages returned to users via REST API
- Validation error messages
- Success messages
- Notification messages (email, SMS)
- Any text that users will read

## What is Forbidden
- ❌ Hardcoded user-facing messages in Java code
- ❌ Concatenating user messages in code
- ❌ Different message text for same error in different places
- ❌ Mixing message keys with actual message text

## Required Pattern

### Message Bundle (Resources)
```properties
# src/main/resources/messages.properties
order.not.found=Order not found with ID: {0}
order.already.submitted=Order {0} has already been submitted
order.validation.empty=Order must contain at least one item
order.limit.exceeded=Order cannot contain more than {0} items
order.created.success=Order {0} created successfully

# src/main/resources/messages_es.properties
order.not.found=Pedido no encontrado con ID: {0}
order.already.submitted=El pedido {0} ya ha sido enviado
```

### Exception with Message Keys
```java
// ✅ CORRECT: Exception with message key
public class OrderNotFoundException extends RuntimeException {
    private static final String MESSAGE_KEY = "order.not.found";
    private final String orderId;
    
    public OrderNotFoundException(String orderId) {
        super(MESSAGE_KEY); // Store key, not message
        this.orderId = orderId;
    }
    
    public String getMessageKey() {
        return MESSAGE_KEY;
    }
    
    public Object[] getMessageArgs() {
        return new Object[]{orderId};
    }
}

// ❌ WRONG: Hardcoded message in exception
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderId) {
        super("Order not found: " + orderId); // Hardcoded!
    }
}
```

### Message Service (Application Layer)
```java
// ✅ CORRECT: Centralized message resolution
public interface MessageService {
    String getMessage(String key, Object... args);
    String getMessage(String key, Locale locale, Object... args);
}

// Implementation in infrastructure layer
@Service
public class MessageServiceImpl implements MessageService {
    private final MessageSource messageSource;
    
    public MessageServiceImpl(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    @Override
    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
```

### Exception Handler with Localization
```java
// ✅ CORRECT: Translate message keys in adapter layer
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final MessageService messageService;
    
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
        String localizedMessage = messageService.getMessage(
            ex.getMessageKey(),
            ex.getMessageArgs()
        );
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessageKey(), localizedMessage));
    }
}

// ❌ WRONG: Returning raw exception message
@ExceptionHandler(OrderNotFoundException.class)
public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(ex.getMessage())); // Not localized!
}
```

### Validation Messages
```java
// ✅ CORRECT: Validation with message keys
public class CreateOrderRequest {
    @NotBlank(message = "{order.customer.name.required}")
    private String customerName;
    
    @Size(min = 1, max = 100, message = "{order.items.size.invalid}")
    private List<OrderItemRequest> items;
}

// Messages file
// messages.properties
order.customer.name.required=Customer name is required
order.items.size.invalid=Order must contain between {min} and {max} items
```

### Success Responses with Messages
```java
// ✅ CORRECT: Response with message key
public record OrderCreatedResponse(
    String orderId,
    String messageKey,
    String message
) {
    public static OrderCreatedResponse create(Order order, MessageService messageService) {
        String messageKey = "order.created.success";
        String message = messageService.getMessage(messageKey, order.getId());
        return new OrderCreatedResponse(order.getId(), messageKey, message);
    }
}

// ❌ WRONG: Hardcoded success message
return new OrderResponse(order.getId(), "Order created successfully");
```

### Configuration-Driven Language Selection
```java
// ✅ CORRECT: Locale from header or configuration
@Configuration
public class LocaleConfiguration implements WebMvcConfigurer {
    
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }
    
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }
}

// ❌ WRONG: Hardcoded locale in code
String message = messageSource.getMessage("key", null, Locale.ENGLISH); // Don't hardcode!
```

## Message Key Naming Convention
```
{entity}.{operation}.{status}
{entity}.{field}.{error}

Examples:
- order.not.found
- order.already.submitted
- order.created.success
- order.items.empty
- user.email.invalid
- payment.amount.exceeded
```

## Enforcement
- NO hardcoded user-facing text in Java files
- All exceptions MUST use message keys
- Message keys MUST be stable across releases
- Default language is English (messages.properties)
- Additional languages go in messages_{locale}.properties
- Domain and application layers use message keys only
- Adapters resolve keys to localized text
