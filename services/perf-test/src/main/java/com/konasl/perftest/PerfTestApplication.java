package com.konasl.perftest;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class PerfTestApplication implements CommandLineRunner {

        private final RestTemplate restTemplate;
        private static final String BASE_URL = "http://localhost";

        // Configuration
        private static final int TOTAL_SCENARIOS = Integer.getInteger("scenarios", 100);
        private static final int RAMP_UP_SECONDS = Integer.getInteger("ramp", 60);

        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failureCount = new AtomicInteger(0);

        public PerfTestApplication() {
                // Configure JDK HttpClient with connection pooling (Java 11+)
                HttpClient httpClient = HttpClient.newBuilder()
                                .version(HttpClient.Version.HTTP_1_1)
                                .connectTimeout(Duration.ofSeconds(30)) // Increased for heavy load
                                .build();

                JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
                factory.setReadTimeout(Duration.ofSeconds(30)); // Increased for heavy load

                this.restTemplate = new RestTemplate(factory);
        }

        public static void main(String[] args) {
                SpringApplication.run(PerfTestApplication.class, args);
        }

        @Override
        public void run(String... args) throws Exception {
                long startTime = System.currentTimeMillis();

                System.out.println(
                                "Starting Performance Test: " + TOTAL_SCENARIOS + " scenarios, " + RAMP_UP_SECONDS
                                                + "s ramp.");

                try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                        double delayMs = (double) RAMP_UP_SECONDS * 1000 / TOTAL_SCENARIOS;

                        for (int i = 0; i < TOTAL_SCENARIOS; i++) {
                                int index = i;
                                executor.submit(() -> runScenario(index));
                                if (delayMs > 0 && delayMs < 1000) {
                                        Thread.sleep((long) delayMs);
                                } else if (delayMs >= 1000) {
                                        Thread.sleep(1000); // Max 1 second delay per thread
                                }
                        }
                }

                long endTime = System.currentTimeMillis();
                long totalTimeMs = endTime - startTime;

                long hours = totalTimeMs / 3600000;
                long minutes = (totalTimeMs % 3600000) / 60000;
                long seconds = (totalTimeMs % 60000) / 1000;

                System.out.println("\n--- Summary ---");
                System.out.println("Total: " + TOTAL_SCENARIOS + " | Success: " + successCount.get() + " | Failure: "
                                + failureCount.get() + " | Total Time: " +
                                String.format("%02d:%02d:%02d", hours, minutes, seconds));
                System.exit(0);
        }

        private void runScenario(int index) {
                String uid = System.currentTimeMillis() + "-" + index + "-" + ThreadLocalRandom.current().nextInt(1000);
                try {
                        // 1. Create User
                        Map user = post(8080, "/api/users",
                                        Map.of("email", "p" + uid + "@test.com", "fullName", "User " + index, "role",
                                                        "CUSTOMER"));
                        String userId = user.get("userId").toString();

                        // 2. Create Product
                        Map prod = post(8081, "/api/catalog/products",
                                        Map.of("name", "Prod " + index, "description", "Test", "sku", "SKU-" + uid));
                        String productId = prod.get("productId").toString();

                        // 3. Set Price
                        post(8082, "/api/pricing/products/" + productId + "/price",
                                        Map.of("amount", 99.99, "currency", "USD"));

                        // 4. Publish
                        post(8081, "/api/catalog/products/" + productId + "/publish", Map.of());

                        // 5. Stock
                        post(8087, "/api/inventory/stock",
                                        Map.of("productId", productId, "initialQuantity", 1000, "lowStockThreshold",
                                                        10));

                        // 6. Cart
                        post(8083, "/api/cart/items",
                                        Map.of("cartId", UUID.randomUUID().toString(), "userId", userId, "productId",
                                                        productId, "quantity", 1, "price", 99.99, "currency", "USD"));

                        // 7. Reserve
                        post(8087, "/api/inventory/stock/" + productId + "/reserve",
                                        Map.of("productId", productId, "reservationId", UUID.randomUUID().toString(),
                                                        "quantity", 1));

                        // 8. Order
                        Map order = post(8084, "/api/orders",
                                        Map.of("userId", userId, "items",
                                                        List.of(Map.of("productId", productId, "quantity", 1))));
                        String orderId = order.get("orderId").toString();

                        // 9. Payment
                        post(8085, "/api/payments", Map.of("orderId", orderId, "amount", 99.99, "currency", "USD"));

                        // 10. Fulfillment
                        Map ship = post(8086, "/api/fulfillment/shipments", Map.of("orderId", orderId));
                        String shipId = ship.get("shipmentId").toString();
                        post(8086, "/api/fulfillment/shipments/" + shipId + "/dispatch",
                                        Map.of("carrier", "UPS", "trackingNumber", "T-" + shipId));

                        successCount.incrementAndGet();
                } catch (Exception e) {
                        System.out.println("\n--- Exception ---\n" + e.getMessage());
                        failureCount.incrementAndGet();
                }
        }

        private Map post(int port, String path, Map body) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                ResponseEntity<Map> resp = restTemplate.postForEntity(BASE_URL + ":" + port + path,
                                new HttpEntity<>(body, headers), Map.class);
                return resp.getBody();
        }
}
