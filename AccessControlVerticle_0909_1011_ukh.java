// 代码生成时间: 2025-09-09 10:11:50
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.VertxContextPRNG;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.JWTAuthHandlerBase;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.jwt..JWT;

/**
 * This Verticle sets up a simple HTTP server that uses JWT authentication for access control.
 */
public class AccessControlVerticle extends AbstractVerticle {

    private JWTAuthHandler jwtAuthHandler;

    @Override
    public void start(Future<Void> startFuture) {
        // Create a router object.
        Router router = Router.router(vertx);

        // Create a JWT Auth handler with a dummy secret.
        String secret = "your-secret"; // Replace this with a secure secret.
        JWTOptions jwtOptions = new JWTOptions().setJWTOptionsAlgorithm("HS256");
        jwtAuthHandler = JWTAuthHandler.create(vertx, secret, jwtOptions);

        // CORS configuration.
        router.route().handler(CorsHandler.create("*"));

        // Body handler to handle JSON requests.
        router.route().handler(BodyHandler.create());

        // Authentication handler for JWT.
        router.route("/api/protected").handler(jwtAuthHandler);

        // Protected route.
        router.post("/api/protected").handler(this::handleProtectedRoute);

        // Create an HTTP server and attach the router.
        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router);

        // Start the server.
        server.listen(config().getInteger("http.port", 8080), result -> {
            if (result.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(result.cause());
            }
        });
    }

    private void handleProtectedRoute(RoutingContext context) {
        // Extract the JWT from the request.
        String token = context.request().getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            context.fail(401); // Unauthorized.
            return;
        }
        token = token.substring(7); // Remove the 'Bearer ' prefix.

        // Verify the token.
        jwtAuthHandler.authenticate(new JsonObject().put("jwt", token), context, res -> {
            if (res.succeeded()) {
                // Authentication succeeded.
                context.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(new JsonObject().put("message", "Access granted").encode());
            } else {
                // Authentication failed.
                context.fail(res.cause());
            }
        });
    }

    // Utility method to create a JWT token.
    public String createJWT(String payload) {
        String secret = "your-secret"; // Replace this with a secure secret.
        JWT jwt = JWT.create();
        jwt.put(payload).putHeader("alg", "HS256");
        return jwt.sign(VertxContextPRNG.randomBytes(32));
    }
}
