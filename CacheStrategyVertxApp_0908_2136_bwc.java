// 代码生成时间: 2025-09-08 21:36:20
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * CacheStrategyVertxApp is a Vertx-based application that demonstrates a simple cache strategy.
 * It caches responses from a mock external service to reduce latency and load on the service.
 */
public class CacheStrategyVertxApp extends AbstractVerticle {

    private static final String CACHE_SERVICE_ENDPOINT = "http://example.com/api/data";
    private static final long CACHE_EXPIRATION_MILLIS = TimeUnit.MINUTES.toMillis(5);
    private ConcurrentHashMap<String, JsonObject> cacheMap;
    private WebClient webClient;

    public void start() throws Exception {
        // Initialize the cache map
        cacheMap = new ConcurrentHashMap<>();
        // Initialize the WebClient for making HTTP requests
        WebClientOptions options = new WebClientOptions();
        webClient = WebClient.create(vertx, options);

        // Create a router object
        Router router = Router.router(vertx);

        // Handle GET requests to the /data endpoint
        router.get("/data").handler(this::handleDataRequest);

        // Serve static files from the webroot directory
        router.route("/").handler(StaticHandler.create("webroot"));

        // Start the web server on port 8080
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getInteger("http.port", 8080));
    }

    private void handleDataRequest(RoutingContext context) {
        String cacheKey = "data";
        cacheMap.computeIfAbsent(cacheKey, k -> {
            // Fetch data from the external service if not present in cache
            fetchExternalData(cacheKey);
            return null;
        });

        JsonObject cachedData = cacheMap.get(cacheKey);
        if (cachedData != null) {
            context.response()
                .putHeader("content-type", "application/json")
                .end(cachedData.encodePrettily());
        } else {
            context.response().setStatusCode(503).end("Service Unavailable");
        }
    }

    private void fetchExternalData(String cacheKey) {
        webClient.get(CACHE_SERVICE_ENDPOINT)
            .send(ar -> {
                if (ar.succeeded()) {
                    JsonObject response = ar.result().bodyAsJsonObject();
                    cacheMap.put(cacheKey, response.copy());
                    // Set a timer to expire the cache entry
                    vertx.setTimer(CACHE_EXPIRATION_MILLIS, id -> cacheMap.remove(cacheKey));
                } else {
                    // Log error and remove the key from cache map to retry fetching data on next request
                    System.err.println("Failed to fetch data from external service: " + ar.cause().getMessage());
                    cacheMap.remove(cacheKey);
                }
            });
    }

    // Entry point for the application
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new CacheStrategyVertxApp());
    }
}
