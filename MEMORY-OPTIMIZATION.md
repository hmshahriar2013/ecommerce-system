# Memory Optimization Configuration

## Overview
This configuration is optimized for running 8 Spring Boot services + performance test on a system with limited available RAM (4GB for all Java processes).

## Memory Allocation Strategy

### Per Service Memory Budget
Each service uses approximately **384MB max heap** + **128MB metaspace** = **~512MB total per service**
- 8 services × 512MB = **4,096MB (4GB)** for all services
- Performance test: **512MB** additional
- **Total Java memory: ~4.5GB**

### JVM Settings Applied (build.gradle)
```
-Xms128m              # Initial heap: 128MB
-Xmx384m              # Maximum heap: 384MB
-XX:MaxMetaspaceSize=128m        # Class metadata limit
-XX:ReservedCodeCacheSize=64m    # JIT compiled code cache
-XX:+UseG1GC                     # G1 Garbage Collector (better for low memory)
-XX:MaxGCPauseMillis=200         # Target GC pause time
-XX:+UseStringDeduplication      # Reduce string memory usage
```

## Spring Boot Optimizations (application.properties)

### 1. Server Thread Pool (Tomcat)
```properties
server.tomcat.threads.max=20              # Max request handler threads
server.tomcat.threads.min-spare=5         # Minimum idle threads
server.tomcat.accept-count=10             # Backlog queue size
server.tomcat.max-connections=50          # Max concurrent connections
```

### 2. Database Connection Pool (HikariCP)
```properties
spring.datasource.hikari.maximum-pool-size=5      # Max DB connections
spring.datasource.hikari.minimum-idle=2           # Min idle connections
spring.datasource.hikari.connection-timeout=20000 # Connection wait timeout (20s)
spring.datasource.hikari.idle-timeout=300000      # Idle connection timeout (5min)
spring.datasource.hikari.max-lifetime=600000      # Max connection lifetime (10min)
```

### 3. Task Execution Thread Pool
```properties
spring.task.execution.pool.core-size=5         # Core thread count
spring.task.execution.pool.max-size=10         # Max thread count
spring.task.execution.pool.queue-capacity=50   # Task queue size
```

### 4. Task Scheduling Thread Pool
```properties
spring.task.scheduling.pool.size=2  # Scheduled task threads
```

### 5. JPA/Hibernate Batching
```properties
spring.jpa.hibernate.jdbc.batch_size=10              # Batch insert/update size
spring.jpa.properties.hibernate.order_inserts=true   # Optimize insert order
spring.jpa.properties.hibernate.order_updates=true   # Optimize update order
```

## Performance Test Optimizations

### Connection Pooling (PerfTestApplication.java)
```java
PoolingHttpClientConnectionManager:
- Max total connections: 200
- Max per route: 50
- Connect timeout: 5 seconds
- Connection request timeout: 5 seconds
```

### Gradual Ramp-Up
- Threads start with calculated delay: `rampSeconds * 1000 / totalScenarios`
- Prevents memory spike from simultaneous thread creation
- Maximum 1 second delay between thread starts

## Recommended Test Parameters

### Conservative (Low Memory)
```bash
./run-performance-test.bat 500 60
# 500 scenarios over 60 seconds
# ~8 new threads per second
# Total memory usage: ~4.5GB
```

### Moderate (If stable)
```bash
./run-performance-test.bat 1000 90
# 1000 scenarios over 90 seconds
# ~11 new threads per second
# Total memory usage: ~5GB
```

### Aggressive (Only if system stable)
```bash
./run-performance-test.bat 2000 120
# 2000 scenarios over 120 seconds
# ~16 new threads per second
# Total memory usage: ~5.5GB
```

**⚠️ Do NOT exceed 2000 scenarios without monitoring memory**

## Memory Monitoring

### Check Current Memory Usage
```bash
./check-memory.bat
```

### Watch Memory in Real-Time (PowerShell)
```powershell
while($true) {
    Clear-Host
    Get-Process java | Select-Object Id, WS, CPU, Threads | Format-Table
    Start-Sleep -Seconds 2
}
```

### System Resource Monitor (Windows)
```
Press Ctrl+Shift+Esc → Performance tab → Memory
```

## Startup Sequence (Recommended)

1. **Close unnecessary applications** to free memory
2. **Start services**:
   ```bash
   ./start-all.bat
   ```
3. **Wait 60 seconds** for all services to initialize
4. **Check memory**:
   ```bash
   ./check-memory.bat
   ```
5. **Run conservative test first**:
   ```bash
   ./run-performance-test.bat 500 60
   ```
6. **If successful, gradually increase**:
   ```bash
   ./run-performance-test.bat 1000 90
   ```

## Troubleshooting

### Services Crash with OutOfMemoryError
**Solution**: Reduce `-Xmx` in build.gradle to 256m per service

### Performance Test OOM
**Solution**: Increase ramp time or decrease scenario count

### Services Won't Start
**Solution**: 
1. Check if ports are already in use
2. Verify available system memory
3. Reduce service memory if needed

### Slow Response Times
**Symptoms**: Requests timing out, high latency
**Causes**: 
- Thread pool exhaustion (increase `max-connections`)
- Database connection pool exhaustion (increase `maximum-pool-size`)
- Memory pressure causing excessive GC

## Expected Results

With optimized settings:
- **8 services running**: ~4GB total memory
- **Performance test**: Additional 512MB
- **Total Java processes**: ~4.5GB
- **Available for OS/other apps**: 11.5GB (on 16GB system)

### Success Metrics
- All services remain UP during test
- No OutOfMemoryError crashes
- Response times < 1 second
- Success rate > 95%
- GC pause times < 200ms

## Advanced Tuning (If Needed)

### Further Reduce Memory Per Service
In `build.gradle`, change:
```groovy
jvmArgs '-Xms96m', '-Xmx256m',  // Even smaller heap
        '-XX:MaxMetaspaceSize=96m'   // Smaller metaspace
```

### Use Serial GC (Lower Memory Overhead)
```groovy
jvmArgs '-Xms128m', '-Xmx384m',
        '-XX:+UseSerialGC'  // Instead of G1GC
```

### Reduce Thread Pools Further
In `application.properties`:
```properties
server.tomcat.threads.max=10
spring.datasource.hikari.maximum-pool-size=3
spring.task.execution.pool.max-size=5
```

## Performance vs Memory Trade-off

| Configuration | Memory per Service | Throughput | Recommended Scenarios |
|--------------|-------------------|------------|----------------------|
| Ultra-Low    | 256MB             | Low        | < 500                |
| **Current**  | **384MB**         | **Medium** | **500-2000**         |
| Standard     | 512MB             | High       | 2000-4000            |
| High         | 768MB             | Very High  | 4000+                |

## Notes
- These settings prioritize **stability over performance**
- Virtual threads (Project Loom) reduce per-thread memory overhead
- H2 in-memory databases consume less memory than external DBs
- Connection pooling prevents connection exhaustion
- Gradual ramp-up prevents memory spikes
