# Backend Microservices Monorepo

A production-ready backend monorepo built with **Java 21**, **Spring Boot 3.4.1**, and **Gradle 8.11**, following strict **Hexagonal Architecture** principles with **DDD**, **CQRS**, and **Event Sourcing** patterns.

## 🎉 Status: ALL SERVICES RUNNING

✅ **8 microservices fully operational**
- All compilation errors fixed
- All services tested and running
- Ready for development and testing

## 🚀 Quick Start

### Start All Services

**Option 1: Batch File (Recommended)**
```batch
start-all.bat
```

**Option 2: PowerShell**
```powershell
.\start-services.ps1
```

Either command will:
1. Build all 8 microservices
2. Start each service in its own command window
3. Services will be ready in 30-60 seconds

### Service URLs
| Service | Port | URL |
|---------|------|-----|
| User Access | 8080 | http://localhost:8080 |
| Catalog | 8081 | http://localhost:8081 |
| Pricing | 8082 | http://localhost:8082 |
| Cart | 8083 | http://localhost:8083 |
| Orders | 8084 | http://localhost:8084 |
| Payments | 8085 | http://localhost:8085 |
| Fulfillment | 8086 | http://localhost:8086 |
| Inventory | 8087 | http://localhost:8087 |

### Test a Service
```bash
# Health check
curl http://localhost:8081/actuator/health

# Example API call (Catalog)
curl http://localhost:8081/api/v1/products
```

## 🏗️ Architecture

This repository implements **Hexagonal Architecture** (Ports and Adapters) with:

### Layer Structure
- **Domain Layer**: Pure business logic, zero framework dependencies
- **Application Layer**: Use cases, command/query handlers, port interfaces
- **Adapter Layer**: REST controllers, repository implementations, external integrations
- **Infrastructure Layer**: Spring Boot configuration, dependency injection

### Design Patterns
- **DDD (Domain-Driven Design)**: Aggregates, value objects, domain events
- **CQRS (Command Query Responsibility Segregation)**: Separate read/write models
- **Event Sourcing**: Events as the source of truth
- **Outbox Pattern**: Reliable event publishing with transactional guarantees

## 📁 Repository Structure

```
.
├── services/                    # 8 Microservices
│   ├── user-access/            # User management (8080)
│   ├── catalog/                # Product catalog (8081)
│   ├── pricing/                # Pricing engine (8082)
│   ├── cart/                   # Shopping cart (8083)
│   ├── orders/                 # Order management (8084)
│   ├── payments/               # Payment processing (8085)
│   ├── fulfillment/            # Order fulfillment (8086)
│   └── inventory/              # Stock management (8087)
├── shared/                     # Shared libraries
│   └── common/                 # Common domain patterns
│       ├── eventsourcing/      # Event sourcing infrastructure
│       ├── cqrs/              # CQRS patterns
│       ├── domain/            # Base domain classes
│       └── outbox/            # Outbox pattern implementation
├── docs/                       # Documentation
│   ├── DDD-CQRS-ES-GUIDE.md   # Pattern guide
│   ├── QUICKSTART.md          # Getting started
│   └── DECISIONS.md           # Architecture decisions
├── .github/
│   ├── copilot-instructions.md # AI development rules
│   └── instructions/          # Language-specific rules
│       ├── java.instructions.md
│       └── springboot.instructions.md
├── start-all.bat              # Start all services (Windows)
├── settings.gradle            # Monorepo configuration
├── build.gradle              # Root build configuration
└── README.md                 # This file
```

### Service Structure (Hexagonal)
Each service follows this structure:
```
services/<service-name>/
  src/main/java/com/konasl/<servicename>/
    ├── domain/              # Pure Java, no frameworks
    │   ├── <Aggregate>.java
    │   ├── <ValueObject>.java
    │   └── <DomainEvent>.java
    ├── application/         # Use cases
    │   ├── command/        # Write operations
    │   ├── query/          # Read operations
    │   └── handler/        # Command/Query handlers
    ├── adapters/
    │   ├── inbound/        # REST controllers, event listeners
    │   └── outbound/       # Repository implementations
    └── infrastructure/      # Spring configuration
## 🚀 Getting Started

### Prerequisites
- **Java 21** (JDK 21)
- **Gradle 8.11** at `D:\E-drive-Software\gradle-8.11`
- Windows OS (batch scripts provided)
- IDE with Java support (IntelliJ IDEA recommended)

### Running All Services (Easiest)

```batch
start-all.bat
```

This will build and start all 8 microservices in separate windows.

### Building Manually

```batch
# Build all microservices
D:\E-drive-Software\gradle-8.11\bin\gradle.bat ^
  :services:catalog:build ^
  :services:cart:build ^
  :services:fulfillment:build ^
  :services:inventory:build ^
  :services:orders:build ^
  :services:payments:build ^
  :services:pricing:build ^
  :services:user-access:build ^
  -x test --no-daemon

# Build specific service
D:\E-drive-Software\gradle-8.11\bin\gradle.bat :services:catalog:build -x test
```

### Running Individual Services

```batch
# Run specific service
D:\E-drive-Software\gradle-8.11\bin\gradle.bat :services:catalog:bootRun --no-daemon
```

### Testing Services

Wait 30-60 seconds for services to start, then test:

#### Automated End-to-End Testing

Run the complete happy path test in a single command:

```powershell
# PowerShell
.\test-e2e-happy-path.ps1

# With verbose output
.\test-e2e-happy-path.ps1 -Verbose
```

```bash
# Bash
chmod +x test-e2e-happy-path.sh
./test-e2e-happy-path.sh

# With verbose output
VERBOSE=true ./test-e2e-happy-path.sh
```

The automated test will:
- ✅ Check all services are healthy
- ✅ Execute the complete happy path flow (12 steps)
- ✅ Validate each API response
- ✅ Report success/failure with color-coded output

See [E2E-TESTING-GUIDE.md](docs/E2E-TESTING-GUIDE.md) for details.

#### Manual API Testing

```bash
# Health checks
curl http://localhost:8080/actuator/health  # User Access
curl http://localhost:8081/actuator/health  # Catalog
curl http://localhost:8082/actuator/health  # Pricing
curl http://localhost:8083/actuator/health  # Cart
curl http://localhost:8084/actuator/health  # Orders
curl http://localhost:8085/actuator/health  # Payments
curl http://localhost:8086/actuator/health  # Fulfillment
curl http://localhost:8087/actuator/health  # Inventory

# Create a user (User Access Service)
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "customer@example.com",
    "fullName": "John Doe",
    "role": "CUSTOMER"
  }'

# Create a product (Catalog Service)
curl -X POST http://localhost:8081/api/catalog/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Premium Laptop",
    "description": "High-performance laptop",
    "sku": "LAPTOP-001"
  }'
```

## 🎯 Key Principles

### 1. Hexagonal Architecture (Ports & Adapters)
```
Infrastructure → Adapters → Application → Domain
```
- **Domain**: Pure business logic, zero dependencies
- **Application**: Use cases, port interfaces
- **Adapters**: REST, repositories, external integrations
- **Infrastructure**: Spring configuration

### 2. Domain-Driven Design (DDD)
- **Aggregates**: Cart, Order, Product, Stock, User
- **Value Objects**: Money, Email, ProductId
- **Domain Events**: ProductCreated, OrderPlaced
- **Repositories**: Aggregate persistence

### 3. CQRS & Event Sourcing
- Commands and queries separated
- Events as source of truth
- EventStore for persistence
- Read model projections

### 4. Code Quality
- Immutability by default
- Constructor injection only
- Comprehensive tests at all layers
- Clear separation of concerns

## 📚 Documentation

- [**Services README**](SERVICES-README.md) - Service ports, API examples, testing guide
- [**DDD-CQRS-ES Guide**](docs/DDD-CQRS-ES-GUIDE.md) - Pattern explanations
- [**Architecture Guide**](docs/ARCHITECTURE.md) - Detailed hexagonal architecture
- [**Decision Records**](docs/DECISIONS.md) - Architectural decisions
- [**GitHub Copilot Instructions**](.github/copilot-instructions.md) - AI development guidelines

## 🛠️ Development Guidelines

### Adding a New Service

1. Create service directory:
   ```
   services/<service-name>/
   ```

2. Add to `settings.gradle`:
   ```groovy
   include 'services:<service-name>'
   ```

3. Create `build.gradle` in service directory

4. Follow hexagonal architecture structure:
   ```
   src/main/java/com/konasl/<servicename>/
     ├── domain/
     ├── application/
     ├── adapters/
     │   ├── inbound/
     │   └── outbound/
     └── infrastructure/
   ```

### Architectural Rules
✅ **DO:**
- Keep domain layer pure Java (no Spring, no frameworks)
- Define port interfaces in application layer
- Implement adapters in adapter layer
- Use Spring Boot only in adapters and infrastructure
- Write tests for each layer
- Use value objects for domain concepts
- Emit domain events for state changes

❌ **DON'T:**
- Add Spring annotations to domain or application layers
- Put business logic in controllers
- Access databases directly from handlers
- Violate dependency rules (dependencies must point inward)
- Skip tests
- Use primitives for domain concepts (use value objects)
- Mutate aggregate state directly (use events)

## 🧪 Testing

```batch
# Run all tests
D:\E-drive-Software\gradle-8.11\bin\gradle.bat test

# Run tests for specific service
D:\E-drive-Software\gradle-8.11\bin\gradle.bat :services:catalog:test

# Run with coverage
D:\E-drive-Software\gradle-8.11\bin\gradle.bat test jacocoTestReport
```

## 📦 Production Build

```batch
# Build executable JARs for all services
D:\E-drive-Software\gradle-8.11\bin\gradle.bat ^
  :services:catalog:bootJar ^
  :services:cart:bootJar ^
  :services:fulfillment:bootJar ^
  :services:inventory:bootJar ^
  :services:orders:bootJar ^
  :services:payments:bootJar ^
  :services:pricing:bootJar ^
  :services:user-access:bootJar

# JARs will be in:
# services/<service-name>/build/libs/<service-name>.jar

# Run a service JAR
java -jar services/catalog/build/libs/catalog.jar
```

## 🔍 What's Implemented

### ✅ Fully Working Services (All 8)
1. **User Access** (8080) - User management with Email/Username validation
2. **Catalog** (8081) - Product catalog with publish/unpublish workflow
3. **Pricing** (8082) - Dynamic pricing engine
4. **Cart** (8083) - Shopping cart with Money value object
5. **Orders** (8084) - Order placement and confirmation
6. **Payments** (8085) - Payment processing
7. **Fulfillment** (8086) - Shipment creation and dispatch
8. **Inventory** (8087) - Stock management with reservations

### ✅ Patterns Implemented
- Event Sourcing with EventStore
- CQRS with CommandHandler and QueryHandler
- Outbox Pattern for reliable event publishing
- Domain Events for all state changes
- Value Objects (Money, Email, Username, IDs)
- Aggregate Roots with event-driven state
- Read Model Projections
- Hexagonal Architecture throughout

### ✅ Common Infrastructure
- EventStore interface and implementations
- OutboxRepository interface and implementations
- CommandHandler/QueryHandler base patterns
- DomainEvent base interface
- AggregateRoot base class
- EventPublisher interface

## 🤝 Contributing

This repository is configured for GitHub Copilot assistance. When contributing:

1. Read [GitHub Copilot Instructions](.github/copilot-instructions.md)
2. Follow hexagonal architecture principles strictly
3. Maintain layer boundaries (no Spring in domain/application)
4. Write tests for new functionality
5. Use Java 21 features (records, pattern matching)
6. Emit domain events for all state changes
7. Use value objects instead of primitives

## 🎓 Learning Resources

- [SERVICES-README.md](SERVICES-README.md) - Quick start and API examples
- [docs/DDD-CQRS-ES-GUIDE.md](docs/DDD-CQRS-ES-GUIDE.md) - Pattern explanations
- [docs/QUICKSTART.md](docs/QUICKSTART.md) - Step-by-step guide
- [.github/copilot-instructions.md](.github/copilot-instructions.md) - Development rules

## 🚦 Current Status

✅ **All services are operational and tested**
- 8 microservices running on ports 8080-8087
- All compilation errors resolved
- Hexagonal architecture fully implemented
- DDD, CQRS, Event Sourcing patterns in place
- Ready for development and extension

## 📝 License

[Add your license here]

## 🙋 Support

For questions:
- Check [SERVICES-README.md](SERVICES-README.md) for API examples
- Review [docs/DECISIONS.md](docs/DECISIONS.md) for architectural choices
- Consult [.github/copilot-instructions.md](.github/copilot-instructions.md) for coding rules

