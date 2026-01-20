# Quick Start Guide

This guide will help you get up and running with the backend monorepo in 5 minutes.

## Prerequisites Checklist

- [ ] Java 21 installed (`java -version` should show 21.x)
- [ ] Gradle 8.11 at `D:\E-drive-Software\gradle-8.11` (or update `gradle/wrapper/gradle-wrapper.properties`)
- [ ] IDE with Java support (IntelliJ IDEA recommended)
- [ ] Git (for version control)

## Step 1: Verify Setup

```bash
# Check Java version
java -version
# Should output: java version "21.x.x"

# Check Gradle version
D:\E-drive-Software\gradle-8.11\bin\gradle.bat --version
# Should output: Gradle 8.11
```

## Step 2: Build the Project

```bash
# Build all modules
D:\E-drive-Software\gradle-8.11\bin\gradle.bat build

# Expected output:
# BUILD SUCCESSFUL in Xs
```

## Step 3: Run Tests

```bash
# Run all tests
D:\\E-drive-Software\\gradle-8.11\\bin\\gradle.bat test

# Run tests for specific service
D:\\E-drive-Software\\gradle-8.11\\bin\\gradle.bat :services:catalog:test
```

## Step 4: Run All Services

**Option 1: Using Batch Script (Recommended)**
```bash
start-all.bat
```

**Option 2: Using PowerShell**
```powershell
.\start-services.ps1
```

All 8 services will start on ports 8080-8087:
- User Access: 8080 ✅
- Catalog: 8081 ✅
- Pricing: 8082 ✅
- Cart: 8083 ✅
- Orders: 8084 ✅
- Payments: 8085 ✅
- Fulfillment: 8086 ✅
- Inventory: 8087 ✅

## Step 5: Test the APIs

Open a new terminal or browser and try these endpoints:

```bash
# Health checks for all services
curl http://localhost:8080/actuator/health  # User Access
curl http://localhost:8081/actuator/health  # Catalog
curl http://localhost:8082/actuator/health  # Pricing
curl http://localhost:8083/actuator/health  # Cart
curl http://localhost:8084/actuator/health  # Orders
curl http://localhost:8085/actuator/health  # Payments
curl http://localhost:8086/actuator/health  # Fulfillment
curl http://localhost:8087/actuator/health  # Inventory

# Example API calls (see E2E-TESTING-GUIDE.md for complete flow)
curl -X POST http://localhost:8080/api/users ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"user@example.com\",\"fullName\":\"John Doe\",\"role\":\"CUSTOMER\"}"

curl -X POST http://localhost:8081/api/catalog/products ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Test Product\",\"description\":\"A test product\",\"sku\":\"TEST-001\"}"
```

## Understanding the Code Structure

### Domain Layer (Pure Business Logic)
```
services/catalog/src/main/java/.../domain/
├── Product.java          # Domain entity with business logic
├── ProductId.java        # Value object
└── ProductStatus.java    # Value object (enum)
```

### Application Layer (Use Cases)
```
services/catalog/src/main/java/.../application/
├── EventStore.java                # Port interface
├── CreateProductCommandHandler.java   # Command handler
└── PublishProductCommandHandler.java  # Command handler
```

### Adapter Layer (Framework Integration)
```
services/catalog/src/main/java/.../adapters/
├── inbound/
│   ├── CatalogRestController.java     # REST API
│   ├── CreateProductRequest.java      # DTO
│   └── ProductResponse.java           # DTO
└── outbound/
    └── PostgresEventStore.java        # Event Store implementation
```

### Infrastructure Layer (Configuration)
```
services/catalog/src/main/java/com/konasl/catalog/infrastructure/
├── CatalogServiceApplication.java  # Main class
└── UseCaseConfiguration.java       # Wiring
```

## Next Steps

### Adding a New Feature

1. **Start with Domain**: Add business logic to domain entities
2. **Create Use Case**: Add use case in application layer
3. **Add Adapter**: Create REST endpoint or repository
4. **Wire It Up**: Configure in infrastructure layer
5. **Write Tests**: Add tests at each layer

### Adding a New Service

```bash
# 1. Create service directory
mkdir services\my-service\src\main\java\com\konasl\myservice

# 2. Add to settings.gradle
echo include 'services:my-service' >> settings.gradle

# 3. Create build.gradle
# (Copy from existing service like catalog and modify)

# 4. Follow hexagonal architecture structure
```

## Common Commands

```bash
# Clean build all services
D:\E-drive-Software\gradle-8.11\bin\gradle.bat clean build

# Run specific service (e.g., Catalog)
D:\E-drive-Software\gradle-8.11\bin\gradle.bat :services:catalog:bootRun

# Run tests for specific service
D:\E-drive-Software\gradle-8.11\bin\gradle.bat :services:catalog:test --info

# Build specific service
D:\E-drive-Software\gradle-8.11\bin\gradle.bat :services:orders:build

# Check for dependency updates
D:\E-drive-Software\gradle-8.11\bin\gradle.bat dependencyUpdates

# Generate coverage report
D:\E-drive-Software\gradle-8.11\bin\gradle.bat test jacocoTestReport
# Report: build/reports/jacoco/test/html/index.html
```

## Troubleshooting

### Issue: "Gradle wrapper not found"
**Solution**: Use the local Gradle installation at `D:\E-drive-Software\gradle-8.11\bin\gradle.bat` or run from project root

### Issue: "Java version mismatch"
**Solution**: Install Java 21 and set `JAVA_HOME` environment variable

### Issue: "Cannot find Gradle distribution"
**Solution**: This project uses local Gradle at `D:\E-drive-Software\gradle-8.11`. Ensure the path exists and GRADLE_HOME is set correctly.

### Issue: "Port 8080 already in use"
**Solution**: Change port in `application.properties`:
```properties
server.port=8081
```

### Issue: "Tests fail with NoClassDefFoundError"
**Solution**: Clean and rebuild:
```bash
D:\E-drive-Software\gradle-8.11\bin\gradle.bat clean test
```

## IDE Setup

### IntelliJ IDEA

1. **Open Project**: File → Open → Select root directory
2. **Import Gradle**: IntelliJ will auto-detect Gradle
3. **Set SDK**: File → Project Structure → Project → SDK: 21
4. **Enable Annotation Processing**: 
   - Settings → Build → Compiler → Annotation Processors
   - ☑ Enable annotation processing

### VS Code

1. **Install Extensions**:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Gradle for Java

2. **Open Project**: File → Open Folder → Select root directory

3. **Configure Java**: 
   - `Ctrl+Shift+P` → "Java: Configure Java Runtime"
   - Set to Java 21

## Learning Resources

- [Architecture Guide](docs/ARCHITECTURE.md) - Deep dive into hexagonal architecture
- [DDD, CQRS, ES Guide](docs/DDD-CQRS-ES-GUIDE.md) - Domain-Driven Design patterns
- [E2E Testing Guide](docs/E2E-TESTING-GUIDE.md) - Complete happy path testing
- [Decision Records](docs/DECISIONS.md) - Why we made certain choices
- [GitHub Copilot Instructions](.github/copilot-instructions.md) - AI assistance guidelines

## Getting Help

1. Check the documentation in `docs/`
2. Review existing service code (Catalog, Cart, Orders)
3. Look at the tests for examples
4. Consult architectural decision records

## Operational Services

All 8 microservices are operational:

1. **User Access** (8080): User registration and authentication ✅
2. **Catalog** (8081): Product catalog management ✅
3. **Pricing** (8082): Dynamic pricing engine ✅
4. **Cart** (8083): Shopping cart operations ✅
5. **Orders** (8084): Order processing ✅
6. **Payments** (8085): Payment handling ✅
7. **Fulfillment** (8086): Shipment tracking ✅
8. **Inventory** (8087): Stock management ✅

For each service:
- Start with domain entities and business rules
- Add use cases for operations
- Implement REST adapters
- Write tests at each layer

---

**Congratulations!** 🎉 You now have a working hexagonal architecture monorepo.

Happy coding! 🚀
