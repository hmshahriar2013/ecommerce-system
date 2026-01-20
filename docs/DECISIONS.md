# Architectural Decision Records

This document captures key architectural decisions made for this backend monorepo.

## ADR-001: Adopt Hexagonal Architecture

**Date**: 2026-01-19

**Status**: ✅ Accepted

**Context**:
We need an architecture that:
- Separates business logic from technical concerns
- Enables testability without frameworks
- Allows flexibility in technology choices
- Supports long-term maintainability

**Decision**:
Adopt Hexagonal Architecture (Ports and Adapters) as the foundational pattern for all services.

**Consequences**:
- ✅ Business logic is framework-independent
- ✅ High testability (domain tests run without Spring)
- ✅ Can swap adapters (e.g., replace REST with GraphQL, JPA with MongoDB)
- ⚠️ Requires discipline to maintain boundaries
- ⚠️ Slightly more boilerplate (DTOs, mappers)
- ⚠️ Learning curve for developers new to the pattern

---

## ADR-002: Use Java 21 as Baseline

**Date**: 2026-01-19

**Status**: ✅ Accepted

**Context**:
Java 21 is the latest LTS (Long-Term Support) release, offering modern language features and performance improvements.

**Decision**:
Require Java 21 for all services. Use modern Java features:
- Records for immutable DTOs
- Pattern matching in switch expressions
- Sealed classes for restricted hierarchies
- Text blocks for multi-line strings
- Virtual threads (when appropriate)

**Consequences**:
- ✅ Access to latest language features
- ✅ Improved code readability and conciseness
- ✅ Long-term support (until 2029)
- ⚠️ Team must be trained on Java 21 features
- ❌ Cannot deploy to environments with Java < 21

---

## ADR-003: Spring Boot 3.x Only in Adapters and Infrastructure

**Date**: 2026-01-19

**Status**: ✅ Accepted

**Context**:
Spring Boot is a powerful framework, but overuse leads to tight coupling and difficult testing.

**Decision**:
Restrict Spring Boot usage to:
- **Adapters**: `@RestController`, `@Repository`, JPA annotations
- **Infrastructure**: `@SpringBootApplication`, `@Configuration`, `@Bean`

Prohibited in:
- **Domain**: No Spring at all
- **Application**: No Spring annotations (use plain constructors)

**Consequences**:
- ✅ Domain and application layers are testable without Spring
- ✅ Business logic is framework-independent
- ✅ Can migrate away from Spring if needed
- ⚠️ Use cases must be manually wired as `@Bean`s
- ⚠️ Cannot use `@Service` or `@Transactional` in application layer

---

## ADR-004: Gradle 8.11 with Groovy DSL

**Date**: 2026-01-19

**Status**: ✅ Accepted

**Context**:
Need a build tool for the monorepo. Options: Maven, Gradle (Groovy), Gradle (Kotlin DSL).

**Decision**:
Use Gradle 8.11 with Groovy DSL for all build scripts.

**Rationale**:
- Groovy DSL is mature and widely documented
- Better IDE support than Kotlin DSL
- Familiar to most Java developers
- Gradle is more flexible than Maven for monorepos

**Consequences**:
- ✅ Powerful build configuration
- ✅ Good monorepo support
- ✅ Wide ecosystem of plugins
- ⚠️ Developers must learn Gradle (if coming from Maven)
- ❌ Groovy DSL is dynamic (less IDE assistance than Kotlin DSL)

---

## ADR-005: Use application.properties Over YAML

**Date**: 2026-01-19

**Status**: ✅ Accepted

**Context**:
Spring Boot supports both `.properties` and `.yml` for configuration.

**Decision**:
Use `application.properties` exclusively. No YAML files.

**Rationale**:
- Simpler format
- Better IDE support for autocomplete
- Easier to parse in scripts
- Avoids YAML indentation errors

**Consequences**:
- ✅ Consistent configuration format
- ✅ Fewer parsing errors
- ⚠️ Less human-readable for nested properties
- ⚠️ Longer property names (e.g., `spring.datasource.url`)

---

## ADR-006: Monorepo Structure

**Date**: 2026-01-19

**Status**: ✅ Accepted

**Context**:
Need to organize multiple backend services. Options: polyrepo vs monorepo.

**Decision**:
Use a monorepo with all services in the `services/` directory.

Structure:
```
services/
  service-a/
  service-b/
  ...
```

**Rationale**:
- Easier to share common code
- Atomic changes across services
- Simplified CI/CD
- Better for code reuse and refactoring

**Consequences**:
- ✅ Simplified dependency management
- ✅ Easier refactoring across services
- ✅ Single source of truth
- ⚠️ Requires discipline in module boundaries
- ⚠️ Potential for larger repository size
- ⚠️ CI builds may take longer (mitigated with caching)

---

## ADR-007: Separate DTOs from Domain Entities

**Date**: 2026-01-19

**Status**: ✅ Accepted

**Context**:
Should we expose domain entities directly via REST APIs?

**Decision**:
Always create separate DTOs for REST APIs. Never expose domain entities directly.

**Rationale**:
- Domain entities may contain business logic
- REST responses should be stable contracts
- Domain entities may change for business reasons
- Avoids coupling API to domain structure

**Consequences**:
- ✅ Clear separation between API and domain
- ✅ API stability independent of domain changes
- ✅ Can optimize DTOs for API performance
- ⚠️ Requires mapping code (fromDomain/toDomain)
- ⚠️ Slightly more code

---

## ADR-008: Constructor Injection Only

**Date**: 2026-01-19

**Status**: ✅ Accepted

**Context**:
Spring supports field injection, setter injection, and constructor injection.

**Decision**:
Use constructor injection exclusively. No `@Autowired` on fields.

**Rationale**:
- Immutable dependencies (final fields)
- Explicit dependencies (clear constructor signature)
- Easier to test (no reflection needed)
- Prevents circular dependencies

**Consequences**:
- ✅ More testable code
- ✅ Explicit dependencies
- ✅ Compile-time safety
- ⚠️ Verbose for classes with many dependencies
- ⚠️ Cannot use field injection shorthand

---

## ADR-009: Use Event Sourcing and PostgreSQL for Persistence

**Date**: 2026-01-19

**Status**: ✅ Accepted (Implemented)

**Context**:
All operational services need reliable persistence with full audit trail.

**Decision**:
Use Event Sourcing with PostgreSQL event stores for all 8 services (User Access, Catalog, Pricing, Cart, Orders, Payments, Fulfillment, Inventory).

**Rationale**:
- Full audit trail of all domain events
- Natural fit with CQRS pattern
- Supports complex business requirements
- Production-ready persistence

**Consequences**:
- ✅ Complete history of all changes
- ✅ Can replay events for debugging
- ✅ Production-ready
- ✅ Reliable persistence across all services
- ⚠️ Requires proper event versioning strategy
- ⚠️ More complex than simple CRUD

---

## ADR-010: GitHub Copilot Integration

**Date**: 2026-01-19

**Status**: ✅ Accepted

**Context**:
Team uses GitHub Copilot for AI-assisted development.

**Decision**:
Create comprehensive Copilot instructions in `.github/` to enforce:
- Hexagonal architecture boundaries
- Java 21 best practices
- Spring Boot usage restrictions
- Code quality standards

**Rationale**:
- Ensures Copilot suggestions align with architecture
- Reduces architectural violations
- Speeds up development with guardrails
- Educates developers through prompts

**Consequences**:
- ✅ Consistent code generation
- ✅ Fewer architectural violations
- ✅ Faster onboarding (Copilot explains patterns)
- ⚠️ Requires maintaining instruction files
- ⚠️ Copilot may still suggest non-compliant code (requires review)

---

## ADR-011: Base Package Convention

**Date**: 2026-01-19

**Status**: ✅ Accepted

**Context**:
Need a consistent package naming convention for all services.

**Decision**:
Use `com.konasl.<servicename>` as the base package for each service.

Example:
- `com.konasl.useraccess`
- `com.konasl.catalog`
- `com.konasl.pricing`
- `com.konasl.cart`
- `com.konasl.orders`
- `com.konasl.payments`
- `com.konasl.fulfillment`
- `com.konasl.inventory`

**Consequences**:
- ✅ Consistent across all services
- ✅ Clear service ownership
- ✅ Avoids package conflicts
- ⚠️ Must be communicated to all developers

---

## ADR-012: Gradle Wrapper with Local Distribution

**Date**: 2026-01-19

**Status**: ✅ Accepted

**Context**:
Gradle can be configured to use:
- Global distribution URL (downloaded from internet)
- Local distribution path (faster, offline-friendly)

**Decision**:
Configure Gradle wrapper to use local distribution at `D:\E-drive-Software\gradle-8.11` by default, but keep global URL commented out for CI/CD.

**Rationale**:
- Faster builds (no download)
- Works offline
- Easy to switch to global URL for CI/CD

**Consequences**:
- ✅ Fast local builds
- ✅ Offline development support
- ⚠️ Developers must have Gradle 8.11 at specified path
- ⚠️ CI/CD must uncomment global URL

---

## ADR-013: Adopt CQRS Pattern

**Date**: 2026-01-19

**Status**: ✅ Accepted

**Context**:
Traditional CRUD operations combine read and write concerns, which can lead to:
- Suboptimal query performance (normalized write models don't match read patterns)
- Complex domain models trying to serve both writes and reads
- Scaling challenges (reads and writes have different scalability needs)

**Decision**:
Implement CQRS (Command Query Responsibility Segregation) across all bounded contexts:
- Commands change state, produce events
- Queries read data from optimized read models
- Separate command and query handlers

**Rationale**:
- Natural fit with Event Sourcing
- Enables independent optimization of read and write paths
- Clear separation of concerns
- Better scalability (read replicas, caching strategies)

**Consequences**:
- ✅ Read and write models can scale independently
- ✅ Queries can use denormalized views optimized for specific use cases
- ✅ Clear intent: commands vs queries
- ⚠️ Eventual consistency between write and read models
- ⚠️ Increased complexity (separate models and handlers)
- ⚠️ Need to manage read model synchronization

---

## ADR-014: Adopt Event Sourcing

**Date**: 2026-01-19

**Status**: ✅ Accepted

**Context**:
Traditional state-based persistence:
- Loses historical information (only current state is stored)
- Makes auditing and debugging difficult
- Cannot reconstruct how aggregate reached current state
- Temporal queries (state at point in time) are complex

**Decision**:
Use Event Sourcing for core aggregates:
- Persist domain events as source of truth
- Rebuild aggregate state from event stream
- Append-only event store
- Use snapshots for performance optimization

**Rationale**:
- Complete audit trail out of the box
- Natural fit with domain-driven design
- Enables temporal queries
- Support for event-driven architecture
- Facilitates integration with other bounded contexts

**Consequences**:
- ✅ Full audit history preserved
- ✅ Can replay events for debugging
- ✅ Enables event-driven integration
- ✅ Natural fit with CQRS
- ⚠️ Cannot directly query current state (must replay or use snapshots)
- ⚠️ Event schema evolution requires versioning strategy
- ⚠️ More complex than traditional CRUD
- ⚠️ Need to handle event store growth (snapshots, archiving)

---

## ADR-015: Use Outbox Pattern for Reliable Messaging

**Date**: 2026-01-19

**Status**: ✅ Accepted

**Context**:
When publishing events to a message broker (RabbitMQ), two operations must succeed:
1. Persist business data to database
2. Publish event to message broker

Without proper coordination, events can be lost if:
- Database commits but message broker is unavailable
- Message broker accepts message but database transaction rolls back

**Decision**:
Implement Outbox Pattern:
- Persist events in outbox table within same database transaction as business data
- Background dispatcher polls outbox and publishes to RabbitMQ
- Mark events as dispatched after successful publish

**Rationale**:
- Guarantees at-least-once delivery
- Atomic persistence (business data + events)
- Resilient to message broker failures
- Industry-proven pattern

**Consequences**:
- ✅ No event loss during failures
- ✅ Atomic persistence guarantees
- ✅ Decouples business transaction from messaging concerns
- ⚠️ At-least-once delivery requires idempotent consumers
- ⚠️ Small delay between persistence and message delivery
- ⚠️ Need to manage outbox table growth (cleanup old events)

---

## ADR-016: Use RabbitMQ for Inter-Service Communication

**Date**: 2026-01-19

**Status**: ✅ Accepted

**Context**:
Bounded contexts need to communicate asynchronously:
- Order service needs to notify inventory service
- Payment events need to trigger fulfillment
- Support needs access to order data

Options considered:
- **Kafka**: High-throughput, event streaming, complex setup
- **RabbitMQ**: Mature, flexible routing, easier to operate
- **AWS SQS/SNS**: Cloud-native, vendor lock-in

**Decision**:
Use RabbitMQ as the message broker for inter-service communication:
- Topic exchanges for event routing
- Dead letter queues for failed messages
- Retry policies with exponential backoff

**Rationale**:
- Proven technology with broad adoption
- Flexible routing patterns (topic, fanout, direct)
- Good balance of features and operational complexity
- Strong Spring AMQP integration
- Can be deployed on-premise or cloud

**Consequences**:
- ✅ Flexible routing capabilities
- ✅ Built-in retry and dead letter queue support
- ✅ Well-documented Spring integration
- ✅ Can run locally for development
- ⚠️ Not as high-throughput as Kafka
- ⚠️ Need to design exchange/queue topology
- ⚠️ Operational overhead (monitoring, cluster management)

---

## ADR-017: Organize Services by Bounded Context

**Date**: 2026-01-19

**Status**: ✅ Accepted (8 services operational)

**Context**:
In a domain-driven design approach, we need clear boundaries around domain models. Services can be organized by:
- Technical layers (all controllers together, all repositories together)
- Bounded contexts (each business domain is separate)
- Features (vertical slices)

**Decision**:
Organize the monorepo into 8 bounded context services (currently operational):
1. **user-access**: Authentication, authorization (Port 8080) ✅
2. **catalog**: Product catalog (Port 8081) ✅
3. **pricing**: Dynamic pricing (Port 8082) ✅
4. **cart**: Shopping cart (Port 8083) ✅
5. **orders**: Order processing (Port 8084) ✅
6. **payments**: Payment processing (Port 8085) ✅
7. **fulfillment**: Shipping and delivery (Port 8086) ✅
8. **inventory**: Stock management (Port 8087) ✅

Each service:
- Has its own database
- Communicates via domain events
- Follows hexagonal architecture internally

**Rationale**:
- Clear domain boundaries
- Independent deployment and scaling
- Team ownership alignment
- Reduced coupling between contexts

**Consequences**:
- ✅ Clear domain boundaries
- ✅ Independent evolution of contexts
- ✅ Teams can own specific bounded contexts
- ✅ Easier to reason about domain complexity
- ⚠️ More inter-service communication
- ⚠️ Eventual consistency between contexts
- ⚠️ Need distributed transaction patterns (saga)

---

## Future Decisions to Make

- [ ] **ADR-018**: Logging strategy (SLF4J, Logback, structured logging)
- [ ] **ADR-019**: Database migration tool (Flyway, Liquibase)
- [ ] **ADR-020**: API documentation (OpenAPI/Swagger, SpringDoc)
- [ ] **ADR-021**: Error handling and exception mapping strategy
- [ ] **ADR-022**: Security and authentication approach (JWT, OAuth2)
- [ ] **ADR-023**: Observability (metrics, tracing, monitoring)
- [ ] **ADR-024**: CI/CD pipeline structure
- [ ] **ADR-025**: Saga pattern for distributed transactions
