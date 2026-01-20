# Spring Boot Development Instructions for GitHub Copilot

## Spring Boot Version
- **Version**: 3.4.x (latest stable in 3.x line)
- **Spring Framework**: 6.x
- **Minimum Java**: 21

## Critical: Framework Usage Restrictions

### WHERE Spring Boot IS Allowed
Spring Boot and its annotations are ONLY permitted in:
1. **Adapter Layer** (`adapters/`)
   - REST controllers (`@RestController`, `@RequestMapping`)
   - Repository implementations (`@Repository`, JPA annotations)
   - External API clients
   - Message consumers/producers

2. **Infrastructure Layer** (`infrastructure/`)
   - Application main class (`@SpringBootApplication`)
   - Configuration classes (`@Configuration`, `@Bean`)
   - Component scanning setup
   - Property configuration

### WHERE Spring Boot is FORBIDDEN
❌ **Domain Layer** (`domain/`)
- NO `@Component`, `@Service`, `@Repository`
- NO `@Autowired`, `@Inject`
- NO `@Entity`, `@Table`, `@Column`
- NO Spring imports at all
- Pure Java only

❌ **Application Layer** (`application/`)
- NO `@Service`, `@Component`
- NO `@Transactional`
- NO dependency injection annotations
- Plain Java classes with constructor injection

## Adapter Layer Patterns

### REST Controllers (Inbound Adapters)
```java
// ✅ DO: Thin controller delegating to use case
@RestController
@RequestMapping("/api/users")
public class UserRestController {
    
    private final CreateUserUseCase createUserUseCase;
    
    public UserRestController(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
    }
    
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        User user = createUserUseCase.execute(request.username(), request.email());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(UserResponse.fromDomain(user));
    }
}

// ❌ DON'T: Business logic in controller
@PostMapping
public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
    // Validation, data transformation, business rules
    if (request.username() == null || request.username().isBlank()) {
        throw new IllegalArgumentException("Username required");
    }
    User user = new User(UUID.randomUUID().toString(), request.username());
    userRepository.save(user);
    return ResponseEntity.ok(UserResponse.fromDomain(user));
}
```

### Repository Implementations (Outbound Adapters)
```java
// ✅ DO: Implement port interface defined in application layer
@Repository
public class JpaUserRepositoryAdapter implements UserRepository {
    
    private final SpringDataUserRepository springDataRepository;
    
    public JpaUserRepositoryAdapter(SpringDataUserRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }
    
    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.fromDomain(user);
        UserEntity saved = springDataRepository.save(entity);
        return saved.toDomain();
    }
}

// ❌ DON'T: Expose JPA entities directly to application layer
```

### DTOs and Mappers
```java
// ✅ DO: Separate DTOs from domain entities
public record CreateUserRequest(String username, String email) {}

public record UserResponse(String id, String username, String email) {
    public static UserResponse fromDomain(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail()
        );
    }
}

// ❌ DON'T: Use domain entities as DTOs
@PostMapping
public ResponseEntity<User> createUser(@RequestBody User user) { // Wrong!
    return ResponseEntity.ok(userService.create(user));
}
```

## Infrastructure Layer Patterns

### Application Entry Point
```java
// ✅ DO: Minimal main class
@SpringBootApplication(scanBasePackages = "com.konasl.servicename")
public class ServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }
}
```

### Use Case Configuration
```java
// ✅ DO: Wire use cases in infrastructure layer
@Configuration
public class UseCaseConfiguration {
    
    @Bean
    public CreateUserUseCase createUserUseCase(UserRepository userRepository) {
        return new CreateUserUseCase(userRepository);
    }
    
    @Bean
    public UpdateUserUseCase updateUserUseCase(UserRepository userRepository) {
        return new UpdateUserUseCase(userRepository);
    }
}
```

## Configuration Properties

### Use application.properties (NOT YAML)
```properties
# ✅ DO: Use properties format
server.port=8080
spring.application.name=sample-service

spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=user
spring.datasource.password=password

logging.level.com.konasl=DEBUG
```

```yaml
# ❌ DON'T: Use YAML format (project requirement)
server:
  port: 8080
spring:
  application:
    name: sample-service
```

## Dependency Injection

### Constructor Injection (Required)
```java
// ✅ DO: Constructor injection
@RestController
public class UserController {
    private final CreateUserUseCase createUserUseCase;
    
    public UserController(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
    }
}

// ❌ DON'T: Field injection
@RestController
public class UserController {
    @Autowired
    private CreateUserUseCase createUserUseCase;
}
```

## Exception Handling

### Global Exception Handler
```java
// ✅ DO: Centralized exception handling in adapter layer
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
            .badRequest()
            .body(new ErrorResponse(ex.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("An error occurred"));
    }
}
```

## Testing

### Controller Tests
```java
// ✅ DO: Test controllers with MockMvc
@WebMvcTest(UserRestController.class)
class UserRestControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CreateUserUseCase createUserUseCase;
    
    @Test
    void shouldCreateUser() throws Exception {
        // given
        User user = User.create("123", "testuser", "test@example.com");
        when(createUserUseCase.execute(any(), any())).thenReturn(user);
        
        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "testuser", "email": "test@example.com"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("123"));
    }
}
```

## What NOT to Do

❌ NO `@Service` or `@Component` in application layer use cases
❌ NO `@Transactional` in use cases (put it in adapter if needed)
❌ NO Spring dependencies in domain or application layers
❌ NO business logic in `@RestController` classes
❌ NO direct exposure of JPA entities via REST endpoints
❌ NO `@Autowired` field injection (use constructor injection)
❌ NO Spring Boot Starter dependencies in domain/application modules
❌ NO framework-specific exceptions in domain layer

## Gradle Dependencies

### Service Module build.gradle
```groovy
// ✅ DO: Modular dependencies
dependencies {
    // Web (adapters only)
    implementation 'org.springframework.boot:spring-boot-starter-web'
    
    // Data JPA (adapters only)
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    
    // Validation (can be used in domain/application with javax.validation)
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // Database
    runtimeOnly 'com.h2database:h2'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

Always enforce hexagonal architecture boundaries when using Spring Boot.
