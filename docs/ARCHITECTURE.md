# Hexagonal Architecture Guide

## Overview

This repository implements **Hexagonal Architecture** (also known as **Ports and Adapters**), a software design pattern that creates a clear separation between business logic and external dependencies.

This architecture is combined with **Domain-Driven Design (DDD)**, **CQRS**, and **Event Sourcing** patterns to build a robust, scalable distributed system. For detailed information on these patterns, see [DDD-CQRS-ES-GUIDE.md](DDD-CQRS-ES-GUIDE.md).

## Core Concepts

### The Hexagon Metaphor

The "hexagon" is just a visual representation. The key idea is:
- **Inside the hexagon**: Your core business logic (domain + application)
- **Outside the hexagon**: Everything else (databases, web frameworks, external APIs)
- **Ports**: Interfaces that define how to interact with the inside
- **Adapters**: Implementations that connect the outside world to the ports

```
┌─────────────────────────────────────────┐
│         INFRASTRUCTURE                  │
│    (Spring Boot, Configuration)         │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│           ADAPTERS                      │
│  ┌─────────────┐      ┌──────────────┐ │
│  │  Inbound    │      │   Outbound   │ │
│  │ (REST API)  │      │ (Database)   │ │
│  └─────────────┘      └──────────────┘ │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│         APPLICATION                     │
│      (Use Cases, Ports)                 │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│           DOMAIN                        │
│      (Business Logic)                   │
└─────────────────────────────────────────┘
```

## Layer Responsibilities

### 1. Domain Layer (`domain/`)

**Purpose**: Contains the core business logic and rules.

**Characteristics**:
- Pure Java code
- No framework dependencies (Spring, JPA, etc.)
- No infrastructure concerns
- Represents business concepts and rules
- Uses domain-driven design patterns

**What Goes Here**:
- Entities (business objects)
- Value Objects
- Enums
- Domain Services (if needed)
- Domain Events
- Business rules and validation

**Example**:
```java
// ✅ Good: Pure domain entity
public class Order {
    private final String id;
    private final List<OrderItem> items;
    private OrderStatus status;
    
    private Order(String id, List<OrderItem> items) {
        this.id = id;
        this.items = List.copyOf(items);
        this.status = OrderStatus.DRAFT;
    }
    
    public static Order create(String id, List<OrderItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        return new Order(id, items);
    }
    
    public void submit() {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Only draft orders can be submitted");
        }
        this.status = OrderStatus.SUBMITTED;
    }
}
```

### 2. Application Layer (`application/`)

**Purpose**: Orchestrates domain objects to fulfill use cases.

**Characteristics**:
- Defines application-specific business logic
- Contains use cases (application services)
- Defines port interfaces
- No framework annotations
- Framework-agnostic

**What Goes Here**:
- Use Cases (operations the application can perform)
- Port interfaces (input/output contracts)
- Application-specific exceptions
- DTOs for use case inputs/outputs (optional)

**Ports**:
- **Input Ports**: Interfaces for use cases (sometimes implicit)
- **Output Ports**: Interfaces for external dependencies (repositories, APIs)

**Example**:
```java
// Output Port (Repository Interface)
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(String id);
}

// Use Case
public class SubmitOrderUseCase {
    private final OrderRepository orderRepository;
    
    public SubmitOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    public Order execute(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        order.submit();
        return orderRepository.save(order);
    }
}
```

### 3. Adapter Layer (`adapters/`)

**Purpose**: Connects the outside world to the application.

**Characteristics**:
- Implements port interfaces
- Uses framework-specific code
- Divided into inbound and outbound

#### 3a. Inbound Adapters (`adapters/inbound/`)

**Purpose**: Receive requests from outside and call use cases.

**Examples**:
- REST Controllers
- GraphQL Resolvers
- Message Queue Consumers
- CLI Commands
- WebSocket Handlers

**Spring Annotations Allowed**: `@RestController`, `@RequestMapping`, `@PostMapping`, etc.

**Example**:
```java
@RestController
@RequestMapping("/api/orders")
public class OrderRestController {
    private final SubmitOrderUseCase submitOrderUseCase;
    
    public OrderRestController(SubmitOrderUseCase submitOrderUseCase) {
        this.submitOrderUseCase = submitOrderUseCase;
    }
    
    @PostMapping("/{id}/submit")
    public ResponseEntity<OrderResponse> submitOrder(@PathVariable String id) {
        Order order = submitOrderUseCase.execute(id);
        return ResponseEntity.ok(OrderResponse.fromDomain(order));
    }
}
```

#### 3b. Outbound Adapters (`adapters/outbound/`)

**Purpose**: Implement output ports to access external resources.

**Examples**:
- JPA Repository Implementations
- REST Client Implementations
- Message Queue Publishers
- File System Adapters
- Email Senders

**Spring Annotations Allowed**: `@Repository`, `@Component`, JPA annotations

**Example**:
```java
@Repository
public class JpaOrderRepository implements OrderRepository {
    private final SpringDataOrderRepository springRepo;
    
    public JpaOrderRepository(SpringDataOrderRepository springRepo) {
        this.springRepo = springRepo;
    }
    
    @Override
    public Order save(Order order) {
        OrderEntity entity = OrderEntity.fromDomain(order);
        OrderEntity saved = springRepo.save(entity);
        return saved.toDomain();
    }
    
    @Override
    public Optional<Order> findById(String id) {
        return springRepo.findById(id)
            .map(OrderEntity::toDomain);
    }
}
```

### 4. Infrastructure Layer (`infrastructure/`)

**Purpose**: Bootstrap and configure the application.

**Characteristics**:
- Spring Boot configuration
- Dependency injection wiring
- Application startup
- Cross-cutting concerns (security, logging)

**What Goes Here**:
- `@SpringBootApplication` main class
- `@Configuration` classes
- `@Bean` definitions for use cases
- Security configuration
- Database configuration

**Example**:
```java
@Configuration
public class UseCaseConfiguration {
    
    @Bean
    public SubmitOrderUseCase submitOrderUseCase(OrderRepository orderRepository) {
        return new SubmitOrderUseCase(orderRepository);
    }
    
    @Bean
    public CreateOrderUseCase createOrderUseCase(OrderRepository orderRepository) {
        return new CreateOrderUseCase(orderRepository);
    }
}
```

## Dependency Rules

### The Golden Rule: Dependencies Point Inward

```
Infrastructure → Adapters → Application → Domain
```

- **Domain** depends on: NOTHING
- **Application** depends on: Domain only
- **Adapters** depend on: Application and Domain
- **Infrastructure** depends on: Everything (wires it all together)

### Why This Matters

1. **Testability**: Domain and application can be tested without frameworks
2. **Flexibility**: Swap adapters without changing business logic
3. **Maintainability**: Changes in frameworks don't affect core logic
4. **Independence**: Business logic is framework-agnostic

## Data Flow

### Inbound Flow (User → System)

1. **User makes request** → Inbound Adapter (REST Controller)
2. **Controller calls** → Use Case (Application Layer)
3. **Use Case orchestrates** → Domain Entities
4. **Use Case persists via** → Output Port Interface
5. **Adapter implements** → Output Port (Database Access)

### Example Flow: Create Order

```
HTTP POST /api/orders
    ↓
OrderRestController (inbound adapter)
    ↓ calls
CreateOrderUseCase (application)
    ↓ uses
Order.create() (domain)
    ↓ persists via
OrderRepository interface (application port)
    ↓ implemented by
JpaOrderRepository (outbound adapter)
    ↓
Database
```

## Common Patterns

### Pattern 1: DTOs vs Domain Entities

**Rule**: Never expose domain entities directly via REST.

```java
// ✅ Good: Separate DTOs
public record CreateOrderRequest(List<OrderItemDTO> items) {}
public record OrderResponse(String id, String status, List<OrderItemDTO> items) {
    public static OrderResponse fromDomain(Order order) {
        // Map domain to DTO
    }
}

// ❌ Bad: Exposing domain entity
@PostMapping
public ResponseEntity<Order> create(@RequestBody Order order) { ... }
```

### Pattern 2: Repository Entities vs Domain Entities

**Rule**: Domain entities should not have JPA annotations.

```java
// Domain Entity (domain layer)
public class Order {
    private String id;
    private List<OrderItem> items;
    // Pure business logic, no JPA
}

// JPA Entity (outbound adapter)
@Entity
@Table(name = "orders")
class OrderEntity {
    @Id
    private String id;
    
    @OneToMany
    private List<OrderItemEntity> items;
    
    // Conversion methods
    public static OrderEntity fromDomain(Order order) { ... }
    public Order toDomain() { ... }
}
```

### Pattern 3: Use Case Composition

**Rule**: Use cases should be single-purpose and composable.

```java
// ✅ Good: Single-purpose use cases
public class CreateOrderUseCase { ... }
public class SubmitOrderUseCase { ... }
public class CancelOrderUseCase { ... }

// ❌ Bad: God-class service
public class OrderService {
    public Order create() { ... }
    public Order submit() { ... }
    public Order cancel() { ... }
    public Order ship() { ... }
    // ... 20 more methods
}
```

## Testing Strategy

### Domain Layer Tests
- Pure unit tests
- No mocking needed
- Fast and reliable

```java
@Test
void shouldTransitionToDraftWhenCreated() {
    Order order = Order.create("123", List.of(item));
    assertEquals(OrderStatus.DRAFT, order.getStatus());
}
```

### Application Layer Tests
- Test use cases with mocked ports
- Verify orchestration logic

```java
@Test
void shouldSubmitOrderWhenFound() {
    // given
    OrderRepository mockRepo = mock(OrderRepository.class);
    Order order = Order.create("123", items);
    when(mockRepo.findById("123")).thenReturn(Optional.of(order));
    
    // when
    SubmitOrderUseCase useCase = new SubmitOrderUseCase(mockRepo);
    Order result = useCase.execute("123");
    
    // then
    assertEquals(OrderStatus.SUBMITTED, result.getStatus());
    verify(mockRepo).save(order);
}
```

### Adapter Layer Tests
- Integration tests with real frameworks
- Test adapter-specific concerns

```java
@WebMvcTest(OrderRestController.class)
class OrderRestControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean SubmitOrderUseCase submitOrderUseCase;
    
    @Test
    void shouldReturnOkWhenOrderSubmitted() throws Exception {
        mockMvc.perform(post("/api/orders/123/submit"))
            .andExpect(status().isOk());
    }
}
```

## Common Mistakes to Avoid

### ❌ Mistake 1: Leaking Framework into Domain
```java
// ❌ Bad: Spring annotation in domain
@Component
public class Order {
    @Autowired
    private OrderRepository repository;
}
```

### ❌ Mistake 2: Business Logic in Controllers
```java
// ❌ Bad: Validation and logic in controller
@PostMapping
public Order create(@RequestBody CreateOrderRequest req) {
    if (req.items().isEmpty()) {
        throw new BadRequestException();
    }
    Order order = new Order();
    order.setItems(req.items());
    return repository.save(order);
}
```

### ❌ Mistake 3: Anemic Domain Model
```java
// ❌ Bad: No behavior, just getters/setters
public class Order {
    private String id;
    private OrderStatus status;
    
    // Only getters and setters
}
```

### ❌ Mistake 4: Bidirectional Dependencies
```java
// ❌ Bad: Application depending on adapter
public class CreateOrderUseCase {
    private final JpaOrderRepository repository; // Concrete adapter!
}
```

## Benefits of Hexagonal Architecture

1. **Testability**: Business logic can be tested without frameworks
2. **Maintainability**: Clear separation of concerns
3. **Flexibility**: Easy to swap technologies
4. **Framework Independence**: Not locked to Spring, JPA, etc.
5. **Domain Focus**: Business rules are explicit and central
6. **Parallel Development**: Teams can work on different layers independently

## References

- [Hexagonal Architecture by Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design by Eric Evans](https://www.domainlanguage.com/ddd/)
