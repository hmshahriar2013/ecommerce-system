# Hexagonal Architecture Visual Guide

## High-Level Overview

```
┌───────────────────────────────────────────────────────────────┐
│                    EXTERNAL WORLD                             │
│  (Users, Databases, Message Queues, External APIs)           │
└───────────────────────────────────────────────────────────────┘
                            ↕
┌───────────────────────────────────────────────────────────────┐
│                   INFRASTRUCTURE LAYER                        │
│           (Spring Boot, Configuration, Wiring)                │
│                                                               │
│  - @SpringBootApplication                                     │
│  - @Configuration classes                                     │
│  - @Bean definitions                                          │
└───────────────────────────────────────────────────────────────┘
                            ↕
┌───────────────────────────────────────────────────────────────┐
│                     ADAPTER LAYER                             │
│         (Connects external world to application)              │
│                                                               │
│  ┌─────────────────────┐       ┌─────────────────────┐      │
│  │  INBOUND ADAPTERS   │       │  OUTBOUND ADAPTERS  │      │
│  │  (Driving)          │       │  (Driven)           │      │
│  │                     │       │                     │      │
│  │  - REST Controllers │       │  - JPA Repositories │      │
│  │  - GraphQL          │       │  - HTTP Clients     │      │
│  │  - Message Consumers│       │  - Message Producers│      │
│  │  - CLI Commands     │       │  - File System      │      │
│  └─────────────────────┘       └─────────────────────┘      │
└───────────────────────────────────────────────────────────────┘
                            ↕
┌───────────────────────────────────────────────────────────────┐
│                   APPLICATION LAYER                           │
│            (Use Cases and Orchestration)                      │
│                                                               │
│  ┌──────────────────┐      ┌──────────────────┐            │
│  │   USE CASES      │      │    PORT          │            │
│  │                  │      │   INTERFACES     │            │
  │  - CreateProduct  │      │                  │            │
  │  - PublishProduct │      │  - EventStore     │            │
  │  - UpdateProduct  │      │  - OutboxRepo     │            │
│  └──────────────────┘      └──────────────────┘            │
└───────────────────────────────────────────────────────────────┘
                            ↕
┌───────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                             │
│              (Pure Business Logic)                            │
│                                                               │
│  - Entities                                                   │
│  - Value Objects                                              │
│  - Domain Services                                            │
│  - Business Rules                                             │
│  - Domain Events                                              │
│                                                               │
│  NO FRAMEWORK DEPENDENCIES                                    │
└───────────────────────────────────────────────────────────────┘
```

## Request Flow Example: Create Product

```
┌─────────┐
│  User   │
└────┬────┘
     │ HTTP POST /api/catalog/products
     ↓
┌────────────────────────────────────┐
│   CatalogRestController (Inbound)  │
│   @RestController                  │
│   - Receives HTTP request          │
│   - Validates request format       │
│   - Converts JSON → DTO            │
└────────────┬───────────────────────┘
             │ calls
             ↓
┌────────────────────────────────────┐
│   CreateProductCommandHandler      │
│   (Application Layer)              │
│   - Orchestrates business logic    │
│   - Calls domain methods           │
└────────────┬───────────────────────┘
             │ uses
             ↓
┌────────────────────────────────────┐
│   Product.create()                 │
│   (Domain Layer)                   │
│   - Validates business rules       │
│   - Creates domain entity          │
└────────────┬───────────────────────┘
             │ returns to
             ↓
┌────────────────────────────────────┐
│   CreateProductCommandHandler      │
│   - Persists via port interface    │
└────────────┬───────────────────────┘
             │ calls
             ↓
┌────────────────────────────────────┐
│   EventStore Interface             │
│   (Port in Application Layer)      │
└────────────┬───────────────────────┘
             │ implemented by
             ↓
┌────────────────────────────────────┐
│   PostgresEventStore               │
│   (Outbound Adapter)               │
│   - Persists events to database    │
│   - Loads event stream             │
└────────────┬───────────────────────┘
             │ returns
             ↓
┌────────────────────────────────────┐
│   CatalogRestController            │
│   - Converts domain → DTO          │
│   - Returns HTTP response          │
└────────────┬───────────────────────┘
             │ HTTP 201 Created
             ↓
┌─────────┐
│  User   │
└─────────┘
```

## Dependency Direction

```
┌─────────────────────────────────────────────────────┐
│                                                     │
│   Infrastructure                                    │
│        ↓ (depends on)                               │
│   Adapters                                          │
│        ↓ (depends on)                               │
│   Application                                       │
│        ↓ (depends on)                               │
│   Domain                                            │
│        ↓ (depends on)                               │
│   NOTHING                                           │
│                                                     │
└─────────────────────────────────────────────────────┘

KEY RULE: Dependencies point INWARD only
```

## Package Structure

```
com.konasl.catalog/
│
├── domain/
│   ├── Product.java                   # Entity
│   ├── ProductId.java                 # Value Object
│   ├── ProductStatus.java             # Value Object
│   └── ProductCreatedEvent.java       # Domain Event
│
├── application/
│   ├── port/                          # Optional subpackage
│   │   ├── input/
│   │   │   └── CreateProductCommand.java  # Command
│   │   └── output/
│   │       ├── EventStore.java            # Output Port Interface
│   │       └── OutboxRepository.java      # Output Port Interface
│   ├── CreateProductCommandHandler.java   # Command Handler
│   ├── PublishProductCommandHandler.java  # Command Handler
│   └── ProductNotFoundException.java      # Application Exception
│
├── adapters/
│   ├── inbound/
│   │   ├── rest/
│   │   │   ├── CatalogRestController.java     # REST Controller
│   │   │   ├── CreateProductRequest.java      # Request DTO
│   │   │   └── ProductResponse.java           # Response DTO
│   │   └── messaging/
│   │       └── ProductEventPublisher.java     # Message Publisher
│   └── outbound/
│       ├── persistence/
│       │   ├── PostgresEventStore.java        # Event Store Impl
│       │   ├── PostgresOutboxRepository.java  # Outbox Repo Impl
│       │   └── EventEntity.java               # JPA Entity
│       └── external/
│           └── PricingServiceClient.java      # HTTP Client
│
└── infrastructure/
    ├── CatalogServiceApplication.java # Main class
    ├── UseCaseConfiguration.java      # Bean configuration
    ├── DatabaseConfiguration.java     # DB config
    └── RabbitMQConfiguration.java     # Messaging config
```

## Layer Responsibilities Matrix

| Layer | Purpose | Allowed Dependencies | Framework Usage | Testing |
|-------|---------|---------------------|-----------------|---------|
| **Domain** | Business logic | NONE | ❌ None | Pure unit tests |
| **Application** | Use cases, orchestration | Domain only | ❌ None | Unit tests with mocks |
| **Adapter** | Connect to external world | Application, Domain | ✅ Spring, JPA | Integration tests |
| **Infrastructure** | Bootstrap, configuration | All layers | ✅ Spring Boot | Integration tests |

## Communication Between Layers

### ✅ ALLOWED
```
REST Controller → Use Case → Domain Entity
                ↓
           Repository Interface (Port)
                ↓
           Repository Implementation (Adapter)
```

### ❌ FORBIDDEN
```
Domain Entity → Spring Framework (NO!)
Use Case → JPA Repository (NO! Use port interface)
Controller → Domain Entity directly (NO! Use use case)
```

## Port Types

### Input Ports (Driving Ports)
- **What**: Interfaces that define what the application can do
- **Where**: Application layer (use case interfaces)
- **Who calls**: Inbound adapters (controllers, message consumers)

### Output Ports (Driven Ports)
- **What**: Interfaces that define what the application needs
- **Where**: Application layer (repository, service interfaces)
- **Who implements**: Outbound adapters (JPA repositories, HTTP clients)

## Adapter Types

### Inbound Adapters (Primary/Driving)
- **Purpose**: Accept input from users/systems
- **Examples**:
  - REST Controllers (`@RestController`)
  - GraphQL Resolvers
  - Message Queue Consumers
  - CLI Commands
  - Scheduled Jobs

### Outbound Adapters (Secondary/Driven)
- **Purpose**: Fulfill output port requirements
- **Examples**:
  - JPA Repository implementations
  - HTTP/REST clients
  - Message Queue producers
  - File system access
  - Email senders

## Key Patterns

### Pattern 1: DTO Mapping
```
Request DTO → Use Case → Domain Entity → Repository → Persistence Entity
                                                           ↓
Response DTO ← Use Case ← Domain Entity ← Repository ← Persistence Entity
```

### Pattern 2: Error Handling
```
Domain Exception → Application Exception → Adapter Exception Handler → HTTP Error Response
```

### Pattern 3: Validation
```
1. Adapter validates format (DTO validation)
2. Use Case validates business rules
3. Domain validates invariants
```

## Benefits Visualization

```
┌─────────────────────────────────────────────┐
│         WITHOUT Hexagonal Arch              │
│                                             │
│  Controller → Service → Repository          │
│       ↓           ↓          ↓              │
│   Everything depends on everything          │
│   Tight coupling, hard to test             │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│          WITH Hexagonal Arch                │
│                                             │
│  Controller → UseCase → Domain              │
│       ↓           ↓                         │
│  Repository Port ← (interface)              │
│       ↓                                     │
│  Repository Impl                            │
│                                             │
│  Clear dependencies, easy to test          │
│  Loose coupling, flexible                   │
└─────────────────────────────────────────────┘
```

## Testing Strategy by Layer

```
┌───────────────────────────────────────────────────┐
│  Domain Layer                                     │
│  Testing: Pure unit tests                         │
│  No mocking, no frameworks                        │
│  Fast, reliable                                   │
└───────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────┐
│  Application Layer                                │
│  Testing: Unit tests with mocked ports            │
│  Mock repositories, external services             │
│  Test orchestration logic                         │
└───────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────┐
│  Adapter Layer                                    │
│  Testing: Integration tests                       │
│  Use Spring Test, MockMvc, Testcontainers        │
│  Test framework integration                       │
└───────────────────────────────────────────────────┘
```

## Common Anti-Patterns to Avoid

### ❌ Anti-Pattern 1: Framework in Domain
```java
// WRONG!
@Entity
public class Product {
    @Id
    private String id;
}
```

### ❌ Anti-Pattern 2: Business Logic in Controller
```java
// WRONG!
@PostMapping
public Product create(@RequestBody Product product) {
    if (product.getName().isBlank()) {
        throw new BadRequestException();
    }
    product.setStatus("PUBLISHED");
    return repository.save(product);
}
```

### ❌ Anti-Pattern 3: Bypassing Use Cases
```java
// WRONG!
@RestController
public class ProductController {
    @Autowired
    private EventStore eventStore; // Direct access!
}
```

## Summary

**Remember**:
1. 🎯 **Domain** = Pure business logic
2. 🔄 **Application** = Use cases + ports
3. 🔌 **Adapters** = Connect to outside world
4. ⚙️ **Infrastructure** = Wire everything together
5. ➡️ **Dependencies** = Always point inward

For more details, see [ARCHITECTURE.md](ARCHITECTURE.md)
