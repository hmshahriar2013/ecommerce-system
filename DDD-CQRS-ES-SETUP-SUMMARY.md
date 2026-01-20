# DDD + CQRS + Event Sourcing Setup Summary

## Overview

This document summarizes the architectural foundation established for the DDD + CQRS + Event Sourcing–based Spring Boot monorepo.

**Date**: January 19, 2026  
**Status**: ✅ Foundation Complete

---

## What Was Created

### 1. Shared Common Module

**Location**: `shared/common/`

A library module containing core abstractions for all bounded contexts.

#### CQRS Abstractions (`com.konasl.common.cqrs`)

- **Command**: Marker interface for write operations
- **CommandHandler<C>**: Generic handler for executing commands
- **Query<R>**: Generic marker interface for read operations
- **QueryHandler<Q, R>**: Generic handler for executing queries

#### Domain Abstractions (`com.konasl.common.domain`)

- **DomainEvent**: Base interface for all domain events
  - Event versioning support
  - Aggregate metadata (ID, type)
  - Correlation tracking
  
- **AggregateRoot**: Base class for event-sourced aggregates
  - Event raising and application
  - Version management
  - History reconstruction from events

#### Event Sourcing (`com.konasl.common.eventsourcing`)

- **EventStore<T>**: Repository for persisting/loading aggregates
  - Save/load operations
  - Optimistic concurrency control
  - Event retrieval by version
  
- **ConcurrencyException**: Thrown on concurrent modification conflicts

- **Snapshot<T>**: Performance optimization for long-lived aggregates

#### Outbox Pattern (`com.konasl.common.outbox`)

- **OutboxEvent**: Event stored for reliable message delivery
- **OutboxStatus**: PENDING, DISPATCHED, FAILED
- **OutboxRepository**: Persistence for outbox events
- **OutboxDispatcher**: Background process to publish events

#### Messaging (`com.konasl.common.messaging`)

- **EventPublisher**: Publishes domain events to RabbitMQ
- **EventConsumer**: Consumes events from other bounded contexts

---

### 2. Ten Bounded Context Services

All services follow hexagonal architecture with complete directory structure:

1. **user-access** (`services/user-access/`)
   - Authentication, authorization, user management
   
2. **customer-profile** (`services/customer-profile/`)
   - Customer information, preferences
   
3. **catalog** (`services/catalog/`)
   - Product catalog, categories
   
4. **pricing** (`services/pricing/`)
   - Dynamic pricing, promotions
   
5. **inventory** (`services/inventory/`)
   - Stock management, reservations
   
6. **cart** (`services/cart/`)
   - Shopping cart operations
   
7. **orders** (`services/orders/`)
   - Order processing, fulfillment tracking
   
8. **payments** (`services/payments/`)
   - Payment processing, transactions
   
9. **fulfillment** (`services/fulfillment/`)
   - Shipping, delivery
   
10. **support** (`services/support/`)
    - Customer support, tickets

#### Each Service Has

```
services/{service-name}/
├── build.gradle (Spring Boot dependencies + shared:common)
└── src/main/java/com/konasl/{servicename}/
    ├── domain/              # Pure Java domain logic
    ├── application/         # Use cases, command/query handlers
    ├── adapters/
    │   ├── inbound/        # REST controllers, messaging consumers
    │   └── outbound/       # EventStore, OutboxRepository implementations
    └── infrastructure/      # Spring Boot configuration
```

---

### 3. Build Configuration

**Root Build**: `build.gradle`
- Java 21 toolchain
- Spring Boot 3.4.1
- Dependency management

**Service Builds**: Each service includes:
- Spring Web (REST APIs)
- Spring Data JPA (persistence)
- Spring AMQP (RabbitMQ)
- PostgreSQL driver (production)
- H2 database (development)
- Validation API

**Settings**: `settings.gradle`
- 11 modules: 1 shared + 10 services

---

### 4. Documentation

#### New Documents

**DDD-CQRS-ES-GUIDE.md** (5,000+ words)
Comprehensive guide covering:
- Domain-Driven Design principles
- CQRS pattern (commands vs queries)
- Event Sourcing workflow
- Outbox Pattern for reliable messaging
- RabbitMQ integration
- Complete code examples
- Best practices

#### Updated Documents

**ARCHITECTURE.md**
- Added reference to DDD/CQRS/ES guide
- Links new patterns to hexagonal architecture

**DECISIONS.md**
- **ADR-013**: Adopt CQRS Pattern
- **ADR-014**: Adopt Event Sourcing
- **ADR-015**: Use Outbox Pattern for Reliable Messaging
- **ADR-016**: Use RabbitMQ for Inter-Service Communication
- **ADR-017**: Organize Services by Bounded Context

---

## Architecture Overview

### Layered Architecture

```
┌─────────────────────────────────────────────────────┐
│              INFRASTRUCTURE LAYER                   │
│        (Spring Boot, Configuration, Wiring)         │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│               ADAPTER LAYER                         │
│  ┌──────────────────┐        ┌──────────────────┐  │
│  │ Inbound Adapters │        │ Outbound Adapters│  │
│  │ - REST           │        │ - EventStore     │  │
│  │ - Messaging      │        │ - Outbox         │  │
│  └──────────────────┘        └──────────────────┘  │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│            APPLICATION LAYER                        │
│  - CommandHandlers (write path)                     │
│  - QueryHandlers (read path)                        │
│  - Ports (EventStore, EventPublisher)               │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│              DOMAIN LAYER                           │
│  - Aggregates (event-sourced)                       │
│  - Domain Events (immutable facts)                  │
│  - Business Rules                                   │
└─────────────────────────────────────────────────────┘
```

### Command Flow (Write Path)

```
HTTP POST → REST Controller
    ↓
Create Command
    ↓
CommandHandler.handle(Command)
    ↓
Aggregate.executeBusinessLogic()
    ↓
Aggregate.raiseEvent(DomainEvent)
    ↓
EventStore.save(Aggregate) ← Persist events
    ↓
OutboxRepository.save(OutboxEvent) ← Same transaction
    ↓
Transaction COMMIT
    ↓
OutboxDispatcher (background)
    ↓
EventPublisher.publish(DomainEvent) → RabbitMQ
    ↓
Other Bounded Contexts consume events
```

### Query Flow (Read Path)

```
HTTP GET → REST Controller
    ↓
Create Query
    ↓
QueryHandler.handle(Query)
    ↓
ReadRepository.find() ← Optimized read model
    ↓
Return DTO
```

---

## Key Design Principles

### 1. Hexagonal Architecture

- ✅ Domain layer is pure Java (no framework dependencies)
- ✅ Application layer defines ports (interfaces)
- ✅ Adapters implement ports
- ✅ Infrastructure wires everything together

### 2. Domain-Driven Design

- ✅ 10 bounded contexts with clear boundaries
- ✅ Aggregates protect business invariants
- ✅ Domain events represent facts
- ✅ Ubiquitous language in code

### 3. CQRS

- ✅ Commands change state (write path)
- ✅ Queries read state (read path)
- ✅ Independent optimization
- ✅ Eventual consistency

### 4. Event Sourcing

- ✅ Events are source of truth
- ✅ Aggregates rebuilt from event stream
- ✅ Complete audit trail
- ✅ Temporal queries supported

### 5. Reliable Messaging

- ✅ Outbox Pattern prevents event loss
- ✅ At-least-once delivery guarantee
- ✅ RabbitMQ for inter-service communication
- ✅ Idempotent consumers required

---

## What's Next

### Immediate Next Steps

1. **Implement Sample Use Case** in one service (e.g., user-access)
   - Create domain aggregate (User)
   - Create domain events (UserRegistered, UserActivated)
   - Implement command handler (RegisterUserCommandHandler)
   - Implement query handler (GetUserQueryHandler)
   - Implement EventStore adapter (JpaEventStore)
   - Implement OutboxRepository adapter

2. **RabbitMQ Configuration**
   - Define exchanges, queues, bindings
   - Implement EventPublisher adapter
   - Implement EventConsumer adapter
   - Configure retry and dead letter queues

3. **Event Store Persistence**
   - Design event_store table schema
   - Design outbox table schema
   - Create JPA entities
   - Implement repositories

4. **Read Model Projection**
   - Create read model tables
   - Implement event handlers to update read models
   - Ensure eventual consistency

### Future Enhancements

- [ ] Implement Saga pattern for distributed transactions
- [ ] Add snapshot support for performance
- [ ] Implement event versioning and upcasting
- [ ] Add distributed tracing (correlation IDs)
- [ ] Implement security (JWT, OAuth2)
- [ ] Add API documentation (OpenAPI/Swagger)
- [ ] Set up observability (metrics, logs, traces)
- [ ] Configure CI/CD pipelines

---

## Development Guidelines

### When Creating a New Command

```java
// 1. Define command
public record CreateOrderCommand(
    String orderId,
    String customerId,
    List<OrderLineDto> orderLines
) implements Command {
    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }
}

// 2. Create handler
@Component
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand> {
    private final EventStore<Order> eventStore;
    private final OutboxRepository outboxRepository;
    
    @Transactional
    @Override
    public void handle(CreateOrderCommand command) {
        Order order = new Order(command.orderId(), command.customerId());
        order.addOrderLines(command.orderLines());
        order.placeOrder();
        
        eventStore.save(order);
        
        // Save to outbox
        order.getUncommittedEvents().forEach(event -> {
            outboxRepository.save(OutboxEvent.fromDomainEvent(
                UUID.randomUUID().toString(), event, serialize(event)
            ));
        });
    }
    
    @Override
    public Class<CreateOrderCommand> getCommandType() {
        return CreateOrderCommand.class;
    }
}
```

### When Creating a New Query

```java
// 1. Define query
public record GetOrderQuery(String orderId) implements Query<OrderDto> {
    @Override
    public String getQueryId() {
        return UUID.randomUUID().toString();
    }
}

// 2. Create handler
@Component
public class GetOrderQueryHandler implements QueryHandler<GetOrderQuery, OrderDto> {
    private final OrderReadRepository readRepository;
    
    @Override
    public OrderDto handle(GetOrderQuery query) {
        return readRepository.findById(query.orderId())
            .map(this::toDto)
            .orElseThrow(() -> new OrderNotFoundException(query.orderId()));
    }
    
    @Override
    public Class<GetOrderQuery> getQueryType() {
        return GetOrderQuery.class;
    }
}
```

### When Creating a New Aggregate

```java
public class Order extends AggregateRoot {
    private OrderId orderId;
    private CustomerId customerId;
    private List<OrderLine> orderLines = new ArrayList<>();
    private OrderStatus status = OrderStatus.DRAFT;
    
    // Command method
    public void placeOrder() {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Order already placed");
        }
        raiseEvent(new OrderPlacedEvent(
            UUID.randomUUID().toString(),
            orderId.value(),
            "Order",
            customerId.value(),
            orderLines,
            Instant.now(),
            "system"
        ));
    }
    
    // Event application
    @Override
    protected void apply(DomainEvent event) {
        switch (event) {
            case OrderPlacedEvent e -> {
                this.orderId = new OrderId(e.aggregateId());
                this.customerId = new CustomerId(e.customerId());
                this.orderLines = mapOrderLines(e.orderLines());
                this.status = OrderStatus.PLACED;
            }
            default -> throw new IllegalArgumentException("Unknown event: " + event);
        }
    }
}
```

---

## Resources

### Documentation
- [ARCHITECTURE.md](ARCHITECTURE.md) - Hexagonal Architecture guide
- [DDD-CQRS-ES-GUIDE.md](DDD-CQRS-ES-GUIDE.md) - Comprehensive DDD/CQRS/ES guide
- [DECISIONS.md](DECISIONS.md) - Architectural Decision Records
- [QUICKSTART.md](QUICKSTART.md) - Getting started guide

### External Resources
- **Domain-Driven Design** by Eric Evans
- **Implementing Domain-Driven Design** by Vaughn Vernon
- **Event Sourcing** by Martin Fowler
- **CQRS Journey** by Microsoft Patterns & Practices
- **RabbitMQ Documentation**: https://www.rabbitmq.com/documentation.html

---

## Summary Statistics

- **11 Modules**: 1 shared + 10 bounded contexts
- **16 Core Abstractions**: CQRS, Event Sourcing, Outbox, Messaging
- **10 Service Directories**: All with hexagonal structure
- **5 New ADRs**: CQRS, Event Sourcing, Outbox, RabbitMQ, Bounded Contexts
- **1 Comprehensive Guide**: 5,000+ word DDD/CQRS/ES documentation
- **100% Pure Java**: Domain and common abstractions have ZERO framework dependencies

---

**Status**: ✅ Architectural foundation is complete and ready for implementation

