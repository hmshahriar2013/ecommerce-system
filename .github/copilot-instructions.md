# GitHub Copilot Instructions for Backend Monorepo

## Repository Overview
This is a backend monorepo built with:
- **Java 21**
- **Spring Boot 3.x**
- **Gradle 8.11**
- **Hexagonal Architecture (Ports & Adapters)**

## Architectural Constraints

### MANDATORY: Hexagonal Architecture Principles
All code MUST follow hexagonal architecture:

1. **Domain Layer** (`domain/`)
   - Pure Java, NO framework dependencies
   - NO Spring annotations
   - NO JPA annotations
   - Business logic and domain rules only

2. **Application Layer** (`application/`)
   - Use cases and orchestration
   - Port interfaces (repository, external services)
   - NO direct dependencies on adapters
   - NO framework annotations

3. **Adapter Layer** (`adapters/`)
   - **Inbound** (`adapters/inbound/`): REST controllers, messaging consumers
   - **Outbound** (`adapters/outbound/`): Repository implementations, external API clients
   - Spring annotations ONLY allowed here
   - Thin adapters that delegate to use cases

4. **Infrastructure Layer** (`infrastructure/`)
   - Spring Boot configuration
   - Dependency wiring
   - Application bootstrapping

### Dependency Rules
```
Infrastructure -> Adapters -> Application -> Domain
```
- Domain depends on NOTHING
- Application depends ONLY on Domain
- Adapters depend on Application and Domain (inward)
- Infrastructure wires everything together

### Code Quality Standards
- Use Java 21 features (records, pattern matching, text blocks)
- Prefer immutability
- Use constructor injection over field injection
- DTOs for adapter layer, separate from domain entities
- Meaningful variable and method names
- Comprehensive JavaDoc for public APIs

### Testing Strategy
- Unit tests for domain logic (pure Java)
- Integration tests for adapters
- Use case tests with mocked repositories

### What NOT to Do
❌ NO Spring annotations in domain or application layers
❌ NO direct database access from use cases
❌ NO business logic in controllers
❌ NO anemic domain models (getters/setters only)
❌ NO "just make it work" shortcuts that violate architecture

### Build and Configuration
- Use Gradle Groovy DSL (NOT Kotlin DSL)
- Use application.properties (NOT YAML)
- Base package: `com.konasl`
- Each service module follows: `com.konasl.<servicename>`

## File Organization
```
services/
  <service-name>/
    src/main/java/com/konasl/<servicename>/
      domain/              # Pure Java, business logic
      application/         # Use cases, ports
      adapters/
        inbound/           # REST, messaging
        outbound/          # DB, external APIs
      infrastructure/      # Spring config, wiring
```

When generating code, always respect these boundaries and principles.
