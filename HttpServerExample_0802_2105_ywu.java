// 代码生成时间: 2025-08-02 21:05:01
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class HttpServerExample extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        // Create a router object to handle the routes
        Router router = Router.router(vertx);

        // Serve static resources from the webroot directory
        router.route().handler(StaticHandler.create("webroot"));

        // Handle GET requests for /
        router.route().handler(this::handleGetRequest);

        // Create the HTTP server and pass the router to it
        HttpServer httpServer = vertx.createHttpServer()
            .requestHandler(router)
            .exceptionHandler(this::handleServerError);

        // Start the server on port 8080
        httpServer.listen(8080, result -> {
            if (result.succeeded()) {
                startFuture.complete();
                System.out.println("HTTP server started on port 8080");
            } else {
                startFuture.fail(result.cause());
                System.out.println("HTTP server startup failed: " + result.cause().getMessage());
            }
        });
    }

    /**
     * Handle GET requests to the server.
     * @param context The routing context for the request.
     */
    private void handleGetRequest(RoutingContext context) {
        HttpServerRequest request = context.request();
        HttpServerResponse response = context.response();

        try {
            // Check if the request is a GET request
            if (request.method() == HttpMethod.GET) {
                // Respond with a simple message
                response
                    .putHeader("content-type", "text/plain")
                    .end("Hello from Vert.x!");
            } else {
                // If not a GET request, respond with 405 Method Not Allowed
                response
                    .setStatusCode(405)
                    .end("Method Not Allowed");
            }
        } catch (Exception e) {
            // Handle any unexpected exceptions
            handleException(context, e);
        }
    }

    /**
     * Handle server errors.
     * @param context The routing context for the error.
     * @param e The exception that occurred.
     */
    private void handleServerError(Throwable e) {
        System.err.println("Server error: " + e.getMessage());
    }

    /**
     * Handle exceptions that occur during request processing.
     * @param context The routing context for the exception.
     * @param e The exception that occurred.
     */
    private void handleException(RoutingContext context, Throwable e) {
        HttpServerResponse response = context.response();
        response
            .setStatusCode(500)
            .putHeader("content-type", "text/plain")
            .end("Internal Server Error: " + e.getMessage());
    }
}
