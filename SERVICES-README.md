# Microservices Platform - Running Services

## ✅ Current Status: ALL SERVICES RUNNING

All **8 microservices** are fully operational and running successfully!

## 🚀 Quick Start

### Start All Services

**Option 1: Batch File (Recommended for Windows)**
```batch
start-all.bat
```

**Option 2: PowerShell Script**
```powershell
.\start-services.ps1
```

Both methods will:
1. Build all 8 microservices
2. Start each service in its own command window
3. Services will be ready in 30-60 seconds

### Stop Services
Close the individual command windows or press `Ctrl+C` in each window.

## 📊 Service Ports

| Service          | Port | Status       | Health Check URL |
|------------------|------|--------------|------------------|
| User Access      | 8080 | ✅ Running  | http://localhost:8080/actuator/health |
| Catalog          | 8081 | ✅ Running  | http://localhost:8081/actuator/health |
| Pricing          | 8082 | ✅ Running  | http://localhost:8082/actuator/health |
| Cart             | 8083 | ✅ Running  | http://localhost:8083/actuator/health |
| Orders           | 8084 | ✅ Running  | http://localhost:8084/actuator/health |
| Payments         | 8085 | ✅ Running  | http://localhost:8085/actuator/health |
| Fulfillment      | 8086 | ✅ Running  | http://localhost:8086/actuator/health |
| Inventory        | 8087 | ✅ Running  | http://localhost:8087/actuator/health |

## 🧪 Testing Services

### Health Checks
```bash
# Check all services (run after services start)
curl http://localhost:8080/actuator/health  # User Access
curl http://localhost:8081/actuator/health  # Catalog
curl http://localhost:8082/actuator/health  # Pricing
curl http://localhost:8083/actuator/health  # Cart
curl http://localhost:8084/actuator/health  # Orders
curl http://localhost:8085/actuator/health  # Payments
curl http://localhost:8086/actuator/health  # Fulfillment
curl http://localhost:8087/actuator/health  # Inventory
```

### Example API Endpoints

#### Catalog Service (8081)
```bash
# Create a product
curl -X POST http://localhost:8081/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sample Product",
    "description": "A test product",
    "price": 99.99
  }'

# Get all products
curl http://localhost:8081/api/v1/products
```

#### Cart Service (8083)
```bash
# Add item to cart
curl -X POST http://localhost:8083/api/v1/cart/add \
  -H "Content-Type: application/json" \
  -d '{
    "cartId": "cart-123",
    "productId": "prod-456",
    "quantity": 2,
    "price": 99.99,
    "currency": "USD"
  }'
```

## 🏗️ Architecture

This is a **DDD+CQRS+ES microservices platform** using:

### Design Patterns
- **Hexagonal Architecture** - Clean separation of domain, application, and adapter layers
- **Event Sourcing** - Events as the source of truth for aggregate state
- **CQRS** - Separate read/write models with command and query handlers
- **Outbox Pattern** - Reliable event publishing with transactional guarantees
- **Domain-Driven Design** - Rich domain models with value objects and aggregates

### Technology Stack
- **Java 21** - Modern LTS with records, pattern matching, sealed classes
- **Spring Boot 3.4.1** - Modern Spring framework
- **Gradle 8.11** - Build automation
- **In-Memory Storage** - For development and testing

## 🔧 Development Workflow

### Build Specific Service
```batch
D:\E-drive-Software\gradle-8.11\bin\gradle.bat :services:SERVICE_NAME:build -x test
```

### Run Specific Service
```batch
D:\E-drive-Software\gradle-8.11\bin\gradle.bat :services:SERVICE_NAME:bootRun --no-daemon
```

### Build All Services
```batch
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
```

## 📝 Recent Fixes Applied

All services have been updated with:
- ✅ Fixed EventStore implementations (proper save/load signatures)
- ✅ Fixed OutboxRepository implementations (deleteDispatchedEventsBefore)
- ✅ Added getCommandType()/getQueryType() to all handlers
- ✅ Fixed aggregate apply() methods (changed from protected to public)
- ✅ Updated to use markEventsAsCommitted() instead of clearUncommittedEvents()
- ✅ Created missing domain value objects (Email, Username, Money)
- ✅ Fixed domain event implementations with proper getter methods
- ✅ Fixed command/query implementations

## 💡 Tips

- **Startup Time**: Services take 30-60 seconds to fully start
- **Logs**: Check individual command windows for service logs
- **Port Conflicts**: Ensure no other applications are using ports 8080-8087
- **Health Checks**: Wait for services to start before testing endpoints
- **Multiple Windows**: Each service runs in its own dedicated window for easy monitoring

## 🎯 Next Steps

Now that all services are running, you can:
1. **Test the APIs** - Use the example curl commands above
2. **Implement Features** - Add new commands, queries, and domain logic
3. **Add Persistence** - Replace in-memory stores with real databases
4. **Add Messaging** - Integrate with message brokers for event publishing
5. **Add API Gateway** - Create a unified entry point for all services
6. **Add Service Discovery** - Implement Eureka or Consul for service registration
7. **Add Distributed Tracing** - Integrate Zipkin or Jaeger for request tracing

## 📚 Documentation

- [Main README](README.md) - Project overview and architecture
- [Architecture Guide](docs/ARCHITECTURE.md) - Detailed hexagonal architecture explanation
- [DDD-CQRS-ES Guide](docs/DDD-CQRS-ES-GUIDE.md) - Domain-Driven Design patterns
- [GitHub Copilot Instructions](.github/copilot-instructions.md) - AI development guidelines
