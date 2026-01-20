# Java 21 Development Instructions for GitHub Copilot

## Java Version Requirements
- **Required Version**: Java 21 (LTS)
- **Language Level**: Enable all Java 21 features
- **Toolchain**: Gradle Java toolchain set to 21

## Java 21 Features to Use

### 1. Records (JEP 395)
Use records for immutable data carriers, especially in adapter layer DTOs:

```java
// ✅ DO: Use records for DTOs
public record CreateUserRequest(String username, String email) {}

// ❌ DON'T: Create verbose POJOs with getters/setters
public class CreateUserRequest {
    private String username;
    private String email;
    // ... getters, setters, equals, hashCode, toString
}
```

### 2. Pattern Matching for switch (JEP 441)
Use pattern matching in switch expressions for cleaner code:

```java
// ✅ DO: Use pattern matching
String formatted = switch (obj) {
    case Integer i -> String.format("int %d", i);
    case Long l -> String.format("long %d", l);
    case Double d -> String.format("double %f", d);
    case String s -> String.format("String %s", s);
    default -> obj.toString();
};

// ❌ DON'T: Use instanceof chains
if (obj instanceof Integer) {
    Integer i = (Integer) obj;
    formatted = String.format("int %d", i);
} else if (obj instanceof Long) {
    // ... more instanceof checks
}
```

### 3. Text Blocks (JEP 378)
Use text blocks for multi-line strings:

```java
// ✅ DO: Use text blocks for SQL, JSON, HTML
String query = """
    SELECT u.id, u.name, u.email
    FROM users u
    WHERE u.status = 'ACTIVE'
    ORDER BY u.created_at DESC
    """;

// ❌ DON'T: Use concatenated strings
String query = "SELECT u.id, u.name, u.email\n" +
               "FROM users u\n" +
               "WHERE u.status = 'ACTIVE'\n";
```

### 4. Sealed Classes (JEP 409)
Use sealed classes for restricted type hierarchies in domain:

```java
// ✅ DO: Use sealed classes for domain hierarchies
public sealed interface PaymentMethod 
    permits CreditCard, DebitCard, BankTransfer {}

public final class CreditCard implements PaymentMethod { }
public final class DebitCard implements PaymentMethod { }
public final class BankTransfer implements PaymentMethod { }
```

### 5. Virtual Threads (Project Loom - Preview in 21)
Consider virtual threads for high-concurrency scenarios:

```java
// ✅ DO: Use virtual threads for I/O-bound tasks
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> fetchUserData(userId));
    executor.submit(() -> fetchOrderData(orderId));
}
```

## Code Quality Standards

### Immutability
Prefer immutable objects:
```java
// ✅ DO: Immutable with records
public record User(String id, String name) {}

// ✅ DO: Immutable domain entities
public class Order {
    private final String id;
    private final List<OrderItem> items;
    
    public Order(String id, List<OrderItem> items) {
        this.id = id;
        this.items = List.copyOf(items); // Defensive copy
    }
    
    public List<OrderItem> getItems() {
        return List.copyOf(items); // Return unmodifiable copy
    }
}
```

### Null Safety
Use `Optional` appropriately:
```java
// ✅ DO: Use Optional for potentially absent values
public Optional<User> findById(String id) {
    return Optional.ofNullable(storage.get(id));
}

// ❌ DON'T: Return null for domain objects
public User findById(String id) {
    return storage.get(id); // Can return null
}
```

### Exception Handling
Use specific exceptions and proper error messages:
```java
// ✅ DO: Specific domain exceptions
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String userId) {
        super("User not found: " + userId);
    }
}

// ❌ DON'T: Generic exceptions with vague messages
throw new RuntimeException("Error");
```

## Package Structure
Follow standard Java conventions:
- Package names: lowercase, no underscores
- Class names: PascalCase
- Method names: camelCase
- Constants: UPPER_SNAKE_CASE

## JavaDoc Standards
Document public APIs:
```java
/**
 * Creates a new user in the system.
 * 
 * @param username the unique username for the user
 * @param email the user's email address
 * @return the created user with assigned ID
 * @throws IllegalArgumentException if username is blank or email is invalid
 */
public User createUser(String username, String email) {
    // implementation
}
```

## What NOT to Do
❌ NO Java 8 or older patterns when Java 21 alternatives exist
❌ NO mutable DTOs (use records)
❌ NO raw types or unchecked warnings
❌ NO `@SuppressWarnings` without justification
❌ NO magic numbers (use constants)
❌ NO empty catch blocks
❌ NO System.out.println (use logging framework)

## Testing Conventions
```java
// ✅ DO: Use JUnit 5 with descriptive names
@Test
@DisplayName("Should create user when valid data provided")
void shouldCreateUser_whenValidDataProvided() {
    // given
    String username = "testuser";
    String email = "test@example.com";
    
    // when
    User user = userService.createUser(username, email);
    
    // then
    assertThat(user).isNotNull();
    assertThat(user.getUsername()).isEqualTo(username);
}
```

When suggesting code, always prefer Java 21 features and modern best practices.
