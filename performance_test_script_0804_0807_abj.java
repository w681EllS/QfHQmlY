// 代码生成时间: 2025-08-04 08:07:35
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class PerformanceTestScript extends AbstractVerticle {

    private static final int PORT = 8080; // Port on which the server will listen
    private static final int WARMUP_REQUESTS = 100; // Number of warm-up requests
    private static final int TEST_REQUESTS = 1000; // Number of test requests
    private static final long TEST_DURATION_MS = 60000; // Test duration in milliseconds
    private static final int CONCURRENT_REQUESTS = 10; // Number of concurrent requests

    private long startTime;
    private int completedRequests;
    private int errorCount;
    private final Object lock = new Object();

    @Override
    public void start() throws Exception {
        // Set up the HTTP server with the specified options
        HttpServerOptions options = new HttpServerOptions().setPort(PORT);
        HttpServer server = vertx.createHttpServer(options);

        // Configure the request handler
        server.requestHandler(this::handleRequest);

        // Start the server
        server.listen(result -> {
            if (result.succeeded()) {
                System.out.println("HTTP server started on port " + PORT);
                performTest();
            } else {
                System.err.println("Failed to start HTTP server: " + result.cause().getMessage());
            }
        });
    }

    private void handleRequest(HttpServerRequest request) {
        try {
            // Simulate some processing
            vertx.setTimer(10, id -> {
                JsonObject response = new JsonObject().put("status", "ok");
                request.response().setStatusCode(200).end(response.encode());
            });
        } catch (Exception e) {
            request.response().setStatusCode(500).end("Internal Server Error");
            synchronized (lock) {
                errorCount++;
            }
        }
    }

    private void performTest() {
        synchronized (lock) {
            completedRequests = 0;
            errorCount = 0;
            startTime = System.currentTimeMillis();
        }

        // Send warm-up requests
        for (int i = 0; i < WARMUP_REQUESTS; i++) {
            sendRequest();
        }

        // Start the actual test
        long endTime = startTime + TEST_DURATION_MS;
        while (System.currentTimeMillis() < endTime) {
            for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
                sendRequest();
            }
            // Throttle the loop to avoid overwhelming the CPU
            Thread.sleep(10);
        }

        // Calculate and print the results
        synchronized (lock) {
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Completed requests: " + completedRequests);
            System.out.println("Error count: " + errorCount);
            System.out.println("Throughput: " + (completedRequests / (duration / 1000.0)) + " requests/second");
        }
    }

    private void sendRequest() {
        vertx.createHttpClient().getNow(PORT, "localhost", "/test", response -> {
            if (response.statusCode() == 200) {
                synchronized (lock) {
                    completedRequests++;
                }
            } else {
                synchronized (lock) {
                    errorCount++;
                }
            }
        });
    }

    public static void main(String[] args) {
        // Create and start the Vertx instance
        VertxOptions options = new VertxOptions();
        Vertx vertx = Vertx.vertx(options);
        vertx.deployVerticle(new PerformanceTestScript(), result -> {
            if (result.succeeded()) {
                System.out.println("Performance test script deployed successfully");
            } else {
                System.err.println("Failed to deploy performance test script: " + result.cause().getMessage());
            }
        });
    }
}
