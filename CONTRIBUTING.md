# Contributing to Backend Monorepo

Thank you for contributing! This guide will help you maintain architectural consistency and code quality.

## Table of Contents
1. [Architecture Rules](#architecture-rules)
2. [Development Workflow](#development-workflow)
3. [Code Standards](#code-standards)
4. [Testing Requirements](#testing-requirements)
5. [Pull Request Process](#pull-request-process)

## Architecture Rules

### ✅ MUST Follow

#### 1. Hexagonal Architecture Boundaries
- **Domain Layer**: Pure Java, NO framework dependencies
- **Application Layer**: NO Spring annotations, only constructor injection
- **Adapter Layer**: Framework code OK, but keep it thin
- **Infrastructure Layer**: Configuration and wiring only

#### 2. Dependency Direction
Dependencies MUST point inward:
```
Infrastructure → Adapters → Application → Domain
```

#### 3. Separation of Concerns
- Domain entities ≠ JPA entities
- REST DTOs ≠ Domain entities
- Controllers delegate to use cases (no business logic)

### ❌ MUST NOT Do

- ❌ NO `@Component`, `@Service` in domain or application layers
- ❌ NO `@Entity`, `@Table` in domain layer
- ❌ NO business logic in controllers
- ❌ NO direct repository calls from controllers
- ❌ NO exposing domain entities via REST
- ❌ NO field injection (`@Autowired` on fields)

## Development Workflow

### Before You Start
1. Pull latest changes: `git pull origin main`
2. Create feature branch: `git checkout -b feature/your-feature-name`
3. Read relevant documentation in `docs/`

### During Development
1. **Start with Domain**: Write domain entities and business logic first
2. **Add Application Layer**: Create use cases and port interfaces
3. **Implement Adapters**: Add controllers or repositories
4. **Wire in Infrastructure**: Configure beans if needed
5. **Write Tests**: Add tests at each layer

### When You're Done
1. Run tests: `./gradlew test`
2. Run build: `./gradlew build`
3. Check code style (if linter configured)
4. Commit with meaningful message
5. Push and create pull request

## Code Standards

### Java 21 Features

✅ **USE**:
```java
// Records for DTOs
public record CreateUserRequest(String username, String email) {}

// Pattern matching
String result = switch (status) {
    case DRAFT -> "Not ready";
    case ACTIVE -> "In progress";
    case COMPLETED -> "Done";
};

// Text blocks
String query = """
    SELECT * FROM users
    WHERE status = 'ACTIVE'
    """;
```

❌ **AVOID**:
```java
// Old-style POJOs with boilerplate
public class CreateUserRequest {
    private String username;
    private String email;
    // ... getters, setters, equals, hashCode
}
```

### Naming Conventions

- **Classes**: PascalCase (`CreateUserUseCase`)
- **Methods**: camelCase (`executeCommand`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_RETRY_COUNT`)
- **Packages**: lowercase (`com.konasl.userservice`)

### Package Structure

Follow this structure for all services:
```
com.konasl.<servicename>/
├── domain/
├── application/
├── adapters/
│   ├── inbound/
│   └── outbound/
└── infrastructure/
```

### Constructor Injection

✅ **DO**:
```java
@RestController
public class UserController {
    private final CreateUserUseCase createUserUseCase;
    
    public UserController(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
    }
}
```

❌ **DON'T**:
```java
@RestController
public class UserController {
    @Autowired
    private CreateUserUseCase createUserUseCase;
}
```

### Immutability

Prefer immutable objects:
```java
// ✅ Immutable domain entity
public class Order {
    private final String id;
    private final List<OrderItem> items;
    
    public Order(String id, List<OrderItem> items) {
        this.id = id;
        this.items = List.copyOf(items);
    }
    
    public List<OrderItem> getItems() {
        return List.copyOf(items); // Defensive copy
    }
}
```

### Error Handling

Use domain-specific exceptions:
```java
// ✅ Domain exception
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String userId) {
        super("User not found: " + userId);
    }
}

// ❌ Generic exception
throw new RuntimeException("Error");
```

## Testing Requirements

### Test Coverage

All code MUST have tests:
- Domain layer: 100% coverage (no excuses)
- Application layer: >90% coverage
- Adapter layer: >80% coverage

### Test Structure

Follow Given-When-Then:
```java
@Test
@DisplayName("Should create user when valid data provided")
void shouldCreateUser_whenValidDataProvided() {
    // given
    String username = "testuser";
    String email = "test@example.com";
    
    // when
    User user = userService.createUser(username, email);
    
    // then
    assertThat(user).isNotNull();
    assertThat(user.getUsername()).isEqualTo(username);
}
```

### Test Naming

Use descriptive names:
- `shouldCreateUser_whenValidDataProvided()`
- `shouldThrowException_whenUsernameIsBlank()`
- `shouldActivateOrder_whenInDraftStatus()`

### Test Isolation

Each test should:
- Be independent (no shared state)
- Test one thing
- Be fast
- Be deterministic

## Pull Request Process

### Before Submitting

- [ ] All tests pass (`./gradlew test`)
- [ ] Build succeeds (`./gradlew build`)
- [ ] Code follows architectural rules
- [ ] New features have tests
- [ ] Documentation updated (if needed)

### PR Description Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] New feature
- [ ] Bug fix
- [ ] Refactoring
- [ ] Documentation

## Architectural Compliance
- [ ] Domain layer has no framework dependencies
- [ ] Use cases don't have Spring annotations
- [ ] Adapters are thin and delegate to use cases
- [ ] DTOs are separate from domain entities

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated (if needed)
- [ ] All tests pass

## Checklist
- [ ] Code follows project conventions
- [ ] Self-reviewed the code
- [ ] Documentation updated
- [ ] No breaking changes (or documented)
```

### Code Review Checklist

Reviewers should check:
- [ ] Architectural boundaries respected
- [ ] Dependencies point inward
- [ ] No framework code in domain/application
- [ ] Tests are comprehensive
- [ ] Code is readable and maintainable
- [ ] No duplicate code
- [ ] Error handling is appropriate

## Common Scenarios

### Adding a New REST Endpoint

1. **Check if use case exists** in application layer
2. If not, **create use case**:
   ```java
   // application/CreateUserUseCase.java
   public class CreateUserUseCase {
       private final UserRepository userRepository;
       
       public User execute(String username, String email) {
           User user = User.create(username, email);
           return userRepository.save(user);
       }
   }
   ```

3. **Create controller** in adapter layer:
   ```java
   // adapters/inbound/UserRestController.java
   @RestController
   @RequestMapping("/api/users")
   public class UserRestController {
       private final CreateUserUseCase createUserUseCase;
       
       @PostMapping
       public ResponseEntity<UserResponse> create(@RequestBody CreateUserRequest req) {
           User user = createUserUseCase.execute(req.username(), req.email());
           return ResponseEntity.ok(UserResponse.fromDomain(user));
       }
   }
   ```

4. **Wire use case** in infrastructure:
   ```java
   // infrastructure/UseCaseConfiguration.java
   @Bean
   public CreateUserUseCase createUserUseCase(UserRepository repo) {
       return new CreateUserUseCase(repo);
   }
   ```

5. **Write tests** at each layer

### Adding Business Logic

⚠️ **IMPORTANT**: Business logic goes in the **domain layer**, NOT in controllers or use cases!

```java
// ✅ CORRECT: Business logic in domain
public class Order {
    public void addItem(OrderItem item) {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Cannot add items to submitted order");
        }
        if (items.size() >= MAX_ITEMS) {
            throw new IllegalStateException("Order cannot have more than " + MAX_ITEMS + " items");
        }
        items.add(item);
    }
}

// ❌ WRONG: Business logic in controller
@PostMapping("/{id}/items")
public void addItem(@PathVariable String id, @RequestBody ItemRequest req) {
    Order order = repository.findById(id);
    if (order.getStatus() != OrderStatus.DRAFT) {
        throw new BadRequestException("Cannot add items");
    }
    // ... more logic here
}
```

### Adding a New Service

1. Create service directory:
   ```
   services/new-service/
   ```

2. Add to `settings.gradle`:
   ```groovy
   include 'services:new-service'
   ```

3. Create `build.gradle` (copy from sample-service)

4. Follow hexagonal structure:
   ```
   src/main/java/com/konasl/newservice/
   ├── domain/
   ├── application/
   ├── adapters/
   │   ├── inbound/
   │   └── outbound/
   └── infrastructure/
   ```

## Getting Help

- Read [ARCHITECTURE.md](docs/ARCHITECTURE.md) for detailed architecture guide
- Check [VISUAL-GUIDE.md](docs/VISUAL-GUIDE.md) for diagrams
- Review [DECISIONS.md](docs/DECISIONS.md) for rationale
- Look at `sample-service` for reference implementation
- Ask in team chat or create an issue

## Resources

- [Hexagonal Architecture by Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Java 21 Documentation](https://docs.oracle.com/en/java/javase/21/)
- [Spring Boot 3 Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)

## License

[Your License]

---

**Thank you for contributing!** 🙏

Remember: When in doubt, favor architectural correctness over convenience. The future you (and your team) will thank you! 🚀
