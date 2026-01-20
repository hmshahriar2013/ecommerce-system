# End-to-End Happy Path Testing Guide

## Overview

This document provides a step-by-step guide for testing the complete e-commerce happy path across all bounded contexts using DDD, CQRS, and Event Sourcing patterns.

## Architecture Summary

### Bounded Contexts (Ports)
- **User Access** (8080): User registration and authentication ✅
- **Catalog** (8081): Product management ✅
- **Pricing** (8082): Product pricing ✅
- **Cart** (8083): Shopping cart operations ✅
- **Orders** (8084): Order placement and management ✅
- **Payments** (8085): Payment processing ✅
- **Fulfillment** (8086): Shipment creation and tracking ✅
- **Inventory** (8087): Stock management ✅

### Event Flow

```
UserCreated → ProductPublished → PriceSet → ItemAddedToCart → 
OrderPlaced → StockReserved → PaymentSuccessful → OrderConfirmed → 
ShipmentCreated → ShipmentDispatched
```

## Complete Happy Path Flow

### Step 1: Create User (User Access Service)

**Purpose**: Register a new customer in the system.

**Endpoint**: `POST http://localhost:8080/api/users`

**Request Body**:
```json
{
  "email": "customer@example.com",
  "fullName": "John Doe",
  "role": "CUSTOMER"
}
```

**Expected Response** (201 Created):
```json
{
  "userId": "generated-uuid",
  "message": "User created successfully"
}
```

**Domain Events Emitted**:
- `UserCreatedEvent`

**Business Rules Validated**:
- Email format is valid
- Full name is not empty
- User starts with ACTIVE status

---

### Step 2: Create Product (Catalog Service)

**Purpose**: Admin creates a new product in the catalog.

**Endpoint**: `POST http://localhost:8081/api/catalog/products`

**Request Body**:
```json
{
  "name": "Premium Laptop",
  "description": "High-performance laptop with 16GB RAM",
  "sku": "LAPTOP-001"
}
```

**Expected Response** (201 Created):
```json
{
  "productId": "generated-uuid",
  "message": "Product created successfully"
}
```

**Domain Events Emitted**:
- `ProductCreatedEvent`

**Business Rules Validated**:
- Name is not empty
- Product starts in DRAFT status

---

### Step 3: Publish Product (Catalog Service)

**Purpose**: Make the product available for purchase.

**Endpoint**: `POST http://localhost:8081/api/catalog/products/{productId}/publish`

**Expected Response** (200 OK):
```json
{
  "productId": "product-uuid",
  "message": "Product published successfully"
}
```

**Domain Events Emitted**:
- `ProductPublishedEvent`

**Business Rules Validated**:
- Product must have name and description
- Status changes from DRAFT to PUBLISHED

---

### Step 4: Set Product Price (Pricing Service)

**Purpose**: Set the selling price for the product.

**Endpoint**: `POST http://localhost:8082/api/pricing/products/{productId}/price`

**Request Body**:
```json
{
  "amount": 1299.99,
  "currency": "USD"
}
```

**Expected Response** (201 Created):
```json
{
  "productId": "product-uuid",
  "message": "Price set successfully"
}
```

**Domain Events Emitted**:
- `PriceSetEvent`

**Business Rules Validated**:
- Amount must be positive
- Currency is provided

---

### Step 5: Add Item to Cart (Cart Service)

**Purpose**: Customer adds product to their shopping cart.

**Endpoint**: `POST http://localhost:8083/api/cart/items`

**Request Body**:
```json
{
  "userId": "user-uuid",
  "productId": "product-uuid",
  "quantity": 2
}
```

**Expected Response** (200 OK):
```json
{
  "message": "Item added to cart successfully"
}
```

**Domain Events Emitted**:
- `ItemAddedToCartEvent`

**Business Rules Validated**:
- Quantity must be positive
- If item exists, quantity is updated

---

### Step 6: Place Order (Orders Service)

**Purpose**: Convert cart into an order.

**Endpoint**: `POST http://localhost:8084/api/orders`

**Request Body**:
```json
{
  "userId": "user-uuid",
  "items": [
    {
      "productId": "product-uuid",
      "quantity": 2
    }
  ]
}
```

**Expected Response** (201 Created):
```json
{
  "orderId": "generated-uuid",
  "status": "PENDING_PAYMENT",
  "message": "Order placed successfully"
}
```

**Domain Events Emitted**:
- `OrderPlacedEvent`

**Business Rules Validated**:
- Order must have at least one item
- Order starts with PENDING_PAYMENT status

**Cross-Context Integration**:
- **Should trigger**: Inventory reservation (when Inventory service is complete)
- **Should listen to**: Price data from Pricing service (for total calculation)

---

### Step 7: Process Payment (Payments Service)

**Purpose**: Process payment for the order.

**Endpoint**: `POST http://localhost:8085/api/payments`

**Request Body**:
```json
{
  "orderId": "order-uuid",
  "amount": 2599.98,
  "currency": "USD"
}
```

**Expected Response** (201 Created):
```json
{
  "paymentId": "generated-uuid",
  "status": "SUCCESSFUL",
  "message": "Payment processed successfully"
}
```

**Domain Events Emitted**:
- `PaymentSuccessfulEvent`

**Business Rules Validated**:
- Amount must be positive
- Payment auto-succeeds for MVP

**Cross-Context Integration**:
- **Should trigger**: Order confirmation via event

---

### Step 8: Confirm Order (Orders Service - Event Handler)

**Purpose**: Update order status after successful payment.

**Trigger**: Consumes `PaymentSuccessfulEvent` from Payments service

**Domain Events Emitted**:
- `OrderConfirmedEvent`

**Status Change**: PENDING_PAYMENT → CONFIRMED

**Cross-Context Integration**:
- **Should trigger**: Shipment creation

---

### Step 9: Create Shipment (Fulfillment Service)

**Purpose**: Create shipment for confirmed order.

**Endpoint**: `POST http://localhost:8086/api/fulfillment/shipments`

**Request Body**:
```json
{
  "orderId": "order-uuid"
}
```

**Expected Response** (201 Created):
```json
{
  "shipmentId": "generated-uuid",
  "trackingNumber": "TRK-ABC12345",
  "status": "READY_TO_SHIP",
  "message": "Shipment created"
}
```

**Domain Events Emitted**:
- `ShipmentCreatedEvent`

**Business Rules Validated**:
- Unique tracking number generated
- Shipment starts with READY_TO_SHIP status

---

### Step 10: Get Order Status (Orders Service)

**Purpose**: Verify order is confirmed.

**Endpoint**: `GET http://localhost:8084/api/orders/{orderId}`

**Expected Response** (200 OK):
```json
{
  "orderId": "order-uuid",
  "userId": "user-uuid",
  "items": [
    {
      "productId": "product-uuid",
      "quantity": 2
    }
  ],
  "totalAmount": "2599.98",
  "status": "CONFIRMED"
}
```

---

### Step 11: Get Shipment Status (Fulfillment Service)

**Purpose**: Track shipment for the order.

**Endpoint**: `GET http://localhost:8086/api/fulfillment/shipments/{shipmentId}`

**Expected Response** (200 OK):
```json
{
  "shipmentId": "shipment-uuid",
  "orderId": "order-uuid",
  "trackingNumber": "TRK-ABC12345",
  "status": "READY_TO_SHIP"
}
```

---

## Current Implementation Status

### ✅ Completed Contexts

1. **Catalog** (Full implementation)
   - Domain layer with Product aggregate
   - CQRS commands and queries
   - REST endpoints
   - Event sourcing with in-memory store
   - Read model projection

2. **User Access** (Domain + Basic REST)
   - User aggregate with role management
   - REST endpoints (stub implementations)
   - **TODO**: Wire command/query handlers

3. **Pricing** (Domain + Basic REST)
   - Price aggregate
   - REST endpoints (stub implementations)
   - **TODO**: Wire command/query handlers

4. **Cart** (Domain + Basic REST)
   - Cart aggregate with items
   - REST endpoints (stub implementations)
   - **TODO**: Wire command/query handlers

5. **Orders** (Domain + Basic REST)
   - Order aggregate with status transitions
   - OrderPlaced and OrderConfirmed events
   - REST endpoints (stub implementations)
   - **TODO**: Wire command/query handlers
   - **TODO**: Event consumer for PaymentSuccessful

6. **Payments** (Domain + Basic REST)
   - Payment aggregate (auto-succeed for MVP)
   - PaymentSuccessful event
   - REST endpoints (stub implementations)
   - **TODO**: Wire command/query handlers

7. **Fulfillment** (Domain + Basic REST)
   - Shipment aggregate with tracking
   - REST endpoints (stub implementations)
   - **TODO**: Wire command/query handlers
   - **TODO**: Event consumer for OrderConfirmed

### ⏳ Partially Complete

1. **Inventory** (Domain layer only)
   - Stock aggregate with reservation logic
   - Business rules implemented
   - **TODO**: Application layer (commands, handlers, queries)
   - **TODO**: Adapters (REST, persistence)
   - **TODO**: Infrastructure (Spring Boot setup)
   - **TODO**: Event consumer for OrderPlaced

---

## Next Steps to Complete E2E Flow

### Phase 1: Wire Command/Query Handlers (All Services)

For each service, implement:

1. **Command Handlers**
   - Inject EventStore port
   - Load/create aggregates
   - Execute business logic
   - Save events

2. **Query Handlers**
   - Inject read repository port
   - Query read model
   - Return DTOs

3. **Controllers**
   - Inject handlers via constructor
   - Delegate to handlers
   - Return appropriate HTTP status

**Priority Order**:
1. Orders (most critical for E2E)
2. Payments
3. Fulfillment
4. Cart
5. Pricing
6. User Access

---

### Phase 2: Implement Event Consumers (Cross-Context Integration)

**Critical Event Flows**:

1. **OrderPlaced → Inventory**
   - Inventory service listens to OrderPlaced
   - Reserves stock for order items
   - Emits StockReserved or InsufficientStock event

2. **PaymentSuccessful → Orders**
   - Orders service listens to PaymentSuccessful
   - Confirms order (PENDING_PAYMENT → CONFIRMED)
   - Emits OrderConfirmed event

3. **OrderConfirmed → Inventory**
   - Inventory service listens to OrderConfirmed
   - Confirms stock reservation (permanent deduction)
   - Emits ReservationConfirmed event

4. **OrderConfirmed → Fulfillment**
   - Fulfillment service listens to OrderConfirmed
   - Creates shipment
   - Emits ShipmentCreated event

**Implementation Pattern** (for each consumer):
```java
@Component
public class OrderEventConsumer implements EventConsumer {
    
    private final CommandHandler<ConfirmOrderCommand> handler;
    
    @EventListener
    public void onPaymentSuccessful(PaymentSuccessfulEvent event) {
        handler.handle(new ConfirmOrderCommand(event.orderId()));
    }
}
```

---

### Phase 3: Complete Inventory Service

**Missing Components**:

1. **Application Layer**
   - `InitializeStockCommand` + Handler
   - `ReserveStockCommand` + Handler
   - `ReleaseReservationCommand` + Handler
   - `ConfirmReservationCommand` + Handler
   - `ReplenishStockCommand` + Handler
   - `GetStockQuery` + Handler

2. **Adapter Layer**
   - `StockCommandController` (REST)
   - `StockQueryController` (REST)
   - `InMemoryStockEventStore`
   - `InMemoryStockReadRepository`
   - `OrderEventConsumer` (listens to OrderPlaced)

3. **Infrastructure Layer**
   - `InventoryApplication` (Spring Boot)
   - `application.properties` (port 8087)
   - Event publisher config

---

### Phase 4: Implement Read Models

Each service needs a read model for queries:

1. **Projection Class** (updates read model from events)
```java
@Component
public class OrderReadModelProjection {
    
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        // Save to read repository
    }
    
    @EventListener
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        // Update read repository
    }
}
```

2. **Read Repository** (in-memory for MVP)
```java
public interface OrderReadRepository {
    Optional<OrderDto> findById(String orderId);
    List<OrderDto> findByUserId(String userId);
}
```

---

### Phase 5: Add Validation and Error Handling

1. **Cross-Context Validation**
   - Cart: Check product availability from Inventory
   - Orders: Validate pricing from Pricing service
   - Payments: Verify order exists and amount matches

2. **Compensating Actions**
   - Payment failure → Release inventory reservation
   - Order cancellation → Release inventory reservation
   - Shipment failure → Update order status

3. **Idempotency**
   - Ensure duplicate events don't cause issues
   - Use event IDs for deduplication

---

## Testing Checklist

### Manual Testing Steps

- [ ] Start all 7 services (User Access, Catalog, Pricing, Cart, Orders, Payments, Fulfillment)
- [ ] Create user and note userId
- [ ] Create product and note productId
- [ ] Publish product
- [ ] Set product price
- [ ] Add item to cart
- [ ] Place order and note orderId
- [ ] Process payment
- [ ] Verify order status is CONFIRMED
- [ ] Create shipment
- [ ] Verify shipment has tracking number

### Event Flow Validation

- [ ] Verify UserCreated event published
- [ ] Verify ProductPublished event published
- [ ] Verify PriceSet event published
- [ ] Verify ItemAddedToCart event published
- [ ] Verify OrderPlaced event published
- [ ] Verify PaymentSuccessful event published
- [ ] Verify OrderConfirmed event published
- [ ] Verify ShipmentCreated event published

### Read Model Consistency

- [ ] User appears in User Access read model
- [ ] Published product appears in Catalog read model
- [ ] Price appears in Pricing read model
- [ ] Cart shows correct items
- [ ] Order shows correct status and items
- [ ] Payment shows SUCCESSFUL status
- [ ] Shipment shows tracking number

---

## Known Limitations (MVP)

1. **Authentication**: No JWT/OAuth2 (hardcoded user context)
2. **Messaging**: Using Spring ApplicationEventPublisher (in-process only)
3. **Persistence**: In-memory stores (data lost on restart)
4. **Error Handling**: Basic validation, no retry logic
5. **Monitoring**: Console logging only
6. **Testing**: Manual testing via REST endpoints
7. **Idempotency**: Not implemented
8. **Compensation**: No saga pattern for failures

---

## Production Readiness Roadmap

### Phase 6: Production Infrastructure (Future)

1. **Messaging**: Replace with RabbitMQ/Kafka
2. **Persistence**: Replace with PostgreSQL
3. **Authentication**: Add Spring Security + JWT
4. **API Gateway**: Add routing and authentication
5. **Service Discovery**: Add Eureka/Consul
6. **Monitoring**: Add Prometheus + Grafana
7. **Tracing**: Add Jaeger/Zipkin
8. **Testing**: Add integration tests
9. **CI/CD**: Add pipeline automation
10. **Documentation**: Add OpenAPI/Swagger

---

## Architectural Patterns Implemented

### Domain-Driven Design (DDD)
- ✅ Bounded contexts with clear boundaries
- ✅ Ubiquitous language in domain models
- ✅ Aggregates as consistency boundaries
- ✅ Value objects for type safety
- ✅ Domain events for state changes

### CQRS (Command Query Responsibility Segregation)
- ✅ Separate command and query models
- ✅ Commands for write operations
- ✅ Queries for read operations
- ✅ Separate controllers for commands/queries
- ✅ Read models optimized for queries

### Event Sourcing
- ✅ Events as source of truth
- ✅ Aggregate reconstruction from events
- ✅ Immutable event records
- ✅ Event versioning support
- ✅ Snapshots for optimization

### Hexagonal Architecture
- ✅ Domain layer isolated (no framework dependencies)
- ✅ Application layer with use cases
- ✅ Adapters for external concerns
- ✅ Infrastructure for Spring Boot config
- ✅ Ports (interfaces) for dependencies

---

## Summary

This guide provides a comprehensive walkthrough of the e-commerce happy path across all bounded contexts. The current implementation includes domain models and REST endpoints for all contexts, with Catalog fully implemented as a reference.

**Next Priority**: Wire command/query handlers for Orders, Payments, and Fulfillment to enable the core E2E flow.
