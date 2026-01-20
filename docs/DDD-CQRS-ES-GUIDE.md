# DDD, CQRS, and Event Sourcing Guide

## Overview

This monorepo implements a **Domain-Driven Design (DDD)** approach with **CQRS (Command Query Responsibility Segregation)** and **Event Sourcing** patterns. This document explains how these patterns work together with Hexagonal Architecture.

## Table of Contents

1. [Domain-Driven Design (DDD)](#domain-driven-design-ddd)
2. [CQRS Pattern](#cqrs-pattern)
3. [Event Sourcing](#event-sourcing)
4. [Outbox Pattern](#outbox-pattern)
5. [Messaging with RabbitMQ](#messaging-with-rabbitmq)
6. [How It All Fits Together](#how-it-all-fits-together)
7. [Code Examples](#code-examples)

---

## Domain-Driven Design (DDD)

### What is DDD?

Domain-Driven Design is an approach to software development that focuses on:
- **Ubiquitous Language**: Common language between developers and domain experts
- **Bounded Contexts**: Clear boundaries around related domain models
- **Aggregates**: Consistency boundaries that protect business invariants
- **Domain Events**: Facts that have happened in the domain

### Bounded Contexts

This monorepo is organized into **8 bounded contexts** (all operational), each representing a distinct business domain:

1. **user-access** (Port 8080): Authentication, authorization, user management ✅
2. **catalog** (Port 8081): Product catalog, categories ✅
3. **pricing** (Port 8082): Dynamic pricing, promotions ✅
4. **cart** (Port 8083): Shopping cart operations ✅
5. **orders** (Port 8084): Order processing, fulfillment tracking ✅
6. **payments** (Port 8085): Payment processing, transactions ✅
7. **fulfillment** (Port 8086): Shipping, delivery ✅
8. **inventory** (Port 8087): Stock management, reservations ✅

Each bounded context is:
- An independent service with its own database
- Communicates with others via domain events
- Has its own domain model and business rules

### Aggregates

Aggregates are the core building blocks:
- **Aggregate Root**: The entry point to an aggregate (e.g., `Order`, `Customer`)
- **Entities**: Objects with unique identity within the aggregate
- **Value Objects**: Immutable objects without identity (e.g., `Address`, `Money`)

**Rules:**
- Only aggregate roots can be referenced from outside
- All changes to the aggregate go through the root
- Aggregates protect business invariants

**Example:**
```java
// Order is the aggregate root
public class Order extends AggregateRoot {
    private OrderId orderId;
    private CustomerId customerId;
    private List<OrderLine> orderLines; // Entities within aggregate
    private OrderStatus status;
    
    public void placeOrder() {
        // Validate business rules
        if (orderLines.isEmpty()) {
            throw new IllegalStateException("Cannot place order without items");
        }
        
        // Emit domain event
        raiseEvent(new OrderPlacedEvent(orderId, customerId, orderLines));
    }
}
```

---

## CQRS Pattern

### What is CQRS?

CQRS separates **write operations (commands)** from **read operations (queries)**:

- **Commands**: Change state, produce events, have side effects
- **Queries**: Read state, never modify data, can be optimized for reads

### Why CQRS?

- **Scalability**: Read and write models can scale independently
- **Optimization**: Queries can use denormalized views
- **Clarity**: Clear separation between behavior and data retrieval
- **Event Sourcing Compatibility**: Natural fit with event-sourced aggregates

### Core Abstractions

#### Commands

Commands represent intentions to change state:

```java
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
```

#### Command Handlers

Command handlers execute business logic:

```java
@Component
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand> {
    
    private final OrderRepository orderRepository;
    
    @Override
    public void handle(CreateOrderCommand command) {
        // Load or create aggregate
        Order order = new Order(command.orderId(), command.customerId());
        
        // Execute business logic
        order.addOrderLines(command.orderLines());
        order.placeOrder();
        
        // Save aggregate (persists events)
        orderRepository.save(order);
    }
    
    @Override
    public Class<CreateOrderCommand> getCommandType() {
        return CreateOrderCommand.class;
    }
}
```

#### Queries

Queries retrieve data without side effects:

```java
public record GetOrderDetailsQuery(
    String orderId
) implements Query<OrderDetailsDto> {
    @Override
    public String getQueryId() {
        return UUID.randomUUID().toString();
    }
}
```

#### Query Handlers

Query handlers retrieve data from read models:

```java
@Component
public class GetOrderDetailsQueryHandler 
    implements QueryHandler<GetOrderDetailsQuery, OrderDetailsDto> {
    
    private final OrderReadRepository readRepository;
    
    @Override
    public OrderDetailsDto handle(GetOrderDetailsQuery query) {
        return readRepository.findById(query.orderId())
            .orElseThrow(() -> new OrderNotFoundException(query.orderId()));
    }
    
    @Override
    public Class<GetOrderDetailsQuery> getQueryType() {
        return GetOrderDetailsQuery.class;
    }
}
```

---

## Event Sourcing

### What is Event Sourcing?

Event Sourcing stores the **history of state changes** as a sequence of events, rather than storing just the current state.

### Key Concepts

- **Events are immutable**: Once written, they never change
- **Events are the source of truth**: Current state is rebuilt by replaying events
- **Append-only storage**: Events are never deleted
- **Temporal queries**: Can reconstruct state at any point in time

### Domain Events

Domain events represent facts that have happened:

```java
public record OrderPlacedEvent(
    String eventId,
    String aggregateId,
    String customerId,
    List<OrderLineDto> orderLines,
    Instant occurredAt,
    String causedBy
) implements DomainEvent {
    
    @Override
    public String getAggregateType() {
        return "Order";
    }
    
    @Override
    public String getEventVersion() {
        return "1.0";
    }
}
```

### Event Store

The event store persists and retrieves events:

```java
@Component
public class JpaEventStore implements EventStore<Order> {
    
    private final EventRepository eventRepository;
    
    @Override
    public void save(Order aggregate) {
        long currentVersion = eventRepository.getLatestVersion(aggregate.getAggregateId());
        
        // Optimistic locking check
        if (currentVersion != aggregate.getVersion() - aggregate.getUncommittedEvents().size()) {
            throw new ConcurrencyException(aggregate.getAggregateId(), 
                aggregate.getVersion(), currentVersion);
        }
        
        // Persist events
        aggregate.getUncommittedEvents().forEach(event -> 
            eventRepository.save(toEntity(event))
        );
        
        aggregate.markEventsAsCommitted();
    }
    
    @Override
    public Order load(String aggregateId) {
        List<DomainEvent> events = eventRepository.findByAggregateId(aggregateId);
        Order order = new Order();
        order.loadFromHistory(events);
        return order;
    }
}
```

### Aggregate Root with Event Sourcing

Aggregates rebuild state from events:

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
        raiseEvent(new OrderPlacedEvent(orderId, customerId, orderLines, Instant.now()));
    }
    
    // Event application (rebuilds state)
    @Override
    protected void apply(DomainEvent event) {
        switch (event) {
            case OrderPlacedEvent e -> {
                this.orderId = e.orderId();
                this.customerId = e.customerId();
                this.orderLines = e.orderLines();
                this.status = OrderStatus.PLACED;
            }
            case OrderShippedEvent e -> {
                this.status = OrderStatus.SHIPPED;
            }
            // ... handle other events
        }
    }
}
```

---

## Outbox Pattern

### What is the Outbox Pattern?

The Outbox Pattern ensures **reliable message delivery** by persisting events in an outbox table within the same transaction as business data.

### Why Outbox Pattern?

**Problem**: Dual writes (database + message broker) are not atomic. If the message broker is unavailable, events can be lost.

**Solution**: 
1. Save events to outbox table in same transaction as business data
2. Background process reads outbox and publishes to message broker
3. Mark events as dispatched after successful publish

### Workflow

```
1. Command Handler executes business logic
   ↓
2. Save aggregate events + outbox events (SAME TRANSACTION)
   ↓
3. Transaction commits
   ↓
4. OutboxDispatcher polls for pending events
   ↓
5. Publish events to RabbitMQ
   ↓
6. Mark events as dispatched
```

### Implementation

```java
@Component
public class OutboxDispatcherImpl implements OutboxDispatcher {
    
    private final OutboxRepository outboxRepository;
    private final EventPublisher eventPublisher;
    
    @Scheduled(fixedDelay = 5000) // Every 5 seconds
    @Override
    public void dispatchPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findPendingEvents(100);
        
        for (OutboxEvent outboxEvent : pendingEvents) {
            try {
                DomainEvent domainEvent = deserialize(outboxEvent.getEventPayload());
                eventPublisher.publish(domainEvent);
                outboxEvent.markAsDispatched();
                outboxRepository.update(outboxEvent);
            } catch (Exception e) {
                outboxEvent.markAsFailed(e.getMessage());
                outboxRepository.update(outboxEvent);
            }
        }
    }
}
```

---

## Messaging with RabbitMQ

### Event Publisher

Publishes events to RabbitMQ:

```java
@Component
public class RabbitMqEventPublisher implements EventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Override
    public void publish(DomainEvent event, String routingKey) {
        rabbitTemplate.convertAndSend(
            "domain-events-exchange",
            routingKey,
            event
        );
    }
}
```

### Event Consumer

Consumes events from other bounded contexts:

```java
@Component
public class OrderEventConsumer implements EventConsumer {
    
    private final UpdateInventoryUseCase updateInventoryUseCase;
    
    @Override
    public void handleEvent(DomainEvent event) {
        if (event instanceof OrderPlacedEvent orderPlaced) {
            // Reserve inventory
            updateInventoryUseCase.reserveStock(orderPlaced.orderLines());
        }
    }
    
    @Override
    public Class<? extends DomainEvent>[] getSupportedEventTypes() {
        return new Class[] { OrderPlacedEvent.class };
    }
}
```

---

## How It All Fits Together

### Command Flow (Write Path)

```
1. REST Controller receives HTTP request
   ↓
2. Controller creates Command
   ↓
3. CommandHandler executes business logic
   ↓
4. Aggregate raises DomainEvents
   ↓
5. EventStore persists events (event sourcing)
   ↓
6. OutboxRepository saves events for messaging
   ↓
7. Transaction commits
   ↓
8. OutboxDispatcher publishes events to RabbitMQ
   ↓
9. Other services consume events and update their read models
```

### Query Flow (Read Path)

```
1. REST Controller receives HTTP request
   ↓
2. Controller creates Query
   ↓
3. QueryHandler retrieves data from read model
   ↓
4. Read model is optimized view (denormalized)
   ↓
5. Return DTO to controller
```

### Architecture Layers

```
┌─────────────────────────────────────────────┐
│         Infrastructure Layer                │
│  (Spring Boot Configuration, Wiring)        │
└─────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────┐
│           Adapter Layer                     │
│  - Inbound: REST, Messaging                 │
│  - Outbound: EventStore, OutboxRepository   │
└─────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────┐
│         Application Layer                   │
│  - CommandHandlers, QueryHandlers           │
│  - Ports (EventStore, EventPublisher)       │
└─────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────┐
│           Domain Layer                      │
│  - Aggregates, Entities, Value Objects      │
│  - Domain Events, Business Rules            │
└─────────────────────────────────────────────┘
```

---

## Code Examples

### Complete Command Execution

```java
// 1. Command
public record PlaceOrderCommand(
    String orderId,
    String customerId,
    List<OrderLineDto> orderLines
) implements Command {
    @Override
    public String getCommandId() {
        return UUID.randomUUID().toString();
    }
}

// 2. Command Handler
@Component
public class PlaceOrderCommandHandler implements CommandHandler<PlaceOrderCommand> {
    
    private final EventStore<Order> eventStore;
    private final OutboxRepository outboxRepository;
    
    @Transactional
    @Override
    public void handle(PlaceOrderCommand command) {
        // Create aggregate
        Order order = new Order(command.orderId(), command.customerId());
        order.addOrderLines(command.orderLines());
        order.placeOrder();
        
        // Save events to event store
        eventStore.save(order);
        
        // Save events to outbox for messaging
        order.getUncommittedEvents().forEach(event -> {
            OutboxEvent outboxEvent = OutboxEvent.fromDomainEvent(
                UUID.randomUUID().toString(),
                event,
                serialize(event)
            );
            outboxRepository.save(outboxEvent);
        });
    }
    
    @Override
    public Class<PlaceOrderCommand> getCommandType() {
        return PlaceOrderCommand.class;
    }
}

// 3. REST Controller (Inbound Adapter)
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private final PlaceOrderCommandHandler commandHandler;
    
    @PostMapping
    public ResponseEntity<Void> placeOrder(@RequestBody PlaceOrderRequest request) {
        PlaceOrderCommand command = new PlaceOrderCommand(
            UUID.randomUUID().toString(),
            request.customerId(),
            request.orderLines()
        );
        
        commandHandler.handle(command);
        
        return ResponseEntity.accepted().build();
    }
}
```

### Complete Query Execution

```java
// 1. Query
public record GetOrderQuery(String orderId) implements Query<OrderDto> {
    @Override
    public String getQueryId() {
        return UUID.randomUUID().toString();
    }
}

// 2. Query Handler
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

// 3. REST Controller
@RestController
@RequestMapping("/api/orders")
public class OrderQueryController {
    
    private final GetOrderQueryHandler queryHandler;
    
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable String orderId) {
        GetOrderQuery query = new GetOrderQuery(orderId);
        OrderDto order = queryHandler.handle(query);
        return ResponseEntity.ok(order);
    }
}
```

---

## Best Practices

### Domain Layer
- ✅ Pure Java, no framework dependencies
- ✅ Aggregates protect business invariants
- ✅ Domain events are immutable and versioned
- ✅ Use value objects for domain concepts

### Application Layer
- ✅ Command handlers validate and execute business logic
- ✅ Query handlers retrieve data from read models
- ✅ Use ports (interfaces) for infrastructure dependencies

### Adapter Layer
- ✅ Implement ports defined in application layer
- ✅ REST controllers delegate to handlers
- ✅ Event consumers trigger use cases

### Event Sourcing
- ✅ Events are append-only, never modified
- ✅ Use snapshots for long-lived aggregates
- ✅ Version events for schema evolution
- ✅ Replay events to rebuild state

### CQRS
- ✅ Commands change state, return void
- ✅ Queries read state, never modify
- ✅ Optimize read models independently
- ✅ Use eventual consistency between write and read models

### Messaging
- ✅ Use Outbox Pattern for reliable delivery
- ✅ Consumers must be idempotent
- ✅ Use correlation IDs for tracing
- ✅ Implement retry logic with exponential backoff

---

## Further Reading

- **Domain-Driven Design** by Eric Evans
- **Implementing Domain-Driven Design** by Vaughn Vernon
- **Event Sourcing** by Martin Fowler
- **CQRS Journey** by Microsoft Patterns & Practices
- **RabbitMQ Patterns** - Official RabbitMQ documentation

