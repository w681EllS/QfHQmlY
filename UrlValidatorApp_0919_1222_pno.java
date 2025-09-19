// 代码生成时间: 2025-09-19 12:22:02
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import java.net.URI;
import java.net.URISyntaxException;

public class UrlValidatorApp extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        Router router = Router.router(vertx);

        // Create a WebClient instance to perform HTTP requests
        WebClient client = WebClient.create(vertx, new WebClientOptions().setDefaultPort(80));

        // Define the route for URL validation
        router.post("/validate").handler(this::validateUrl);

        // Start the HTTP server on port 8080
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080, result -> {
                if (result.succeeded()) {
                    startFuture.complete();
                } else {
                    startFuture.fail(result.cause());
                }
            });
    }

    // Handler method for validating URLs
    private void validateUrl(RoutingContext context) {
        // Retrieve the URL from the request body
        JsonObject requestBody = context.getBodyAsJson();
        String url = requestBody.getString("url");

        if (url == null || url.isEmpty()) {
            // Respond with an error if the URL is not provided
            context.response().setStatusCode(400).end("URL is required");
            return;
        }

        // Check if the URL is valid using the URI class
        try {
            new URI(url);
            // Send a HEAD request to check if the URL is reachable
            client.request(HttpMethod.HEAD, url)
                .as(BodyCodec.none())
                .send().onComplete(ar -> {
                    if (ar.succeeded()) {
                        context.response().setStatusCode(200).end("URL is valid");
                    } else {
                        // If the request fails, respond with an error
                        context.response().setStatusCode(500).end("URL validation failed");
                    }
                });
        } catch (URISyntaxException e) {
            // If the URL is not a valid URI, respond with an error
            context.response().setStatusCode(400).end("Invalid URL format");
        }
    }

    // Main method to start the Vert.x application
    public static void main(String[] args) {
        // Deploy the Verticle to the Vert.x instance
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new UrlValidatorApp(), result -> {
            if (result.succeeded()) {
                System.out.println("URL Validator application is running");
            } else {
                System.err.println("Failed to start URL Validator application");
            }
        });
    }
}
