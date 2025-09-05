// 代码生成时间: 2025-09-06 03:38:22
 * A Vert.x application that calculates hash values for input strings.
 *
 * @author Your Name
 * @version 1.0
 */
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashValueCalculator extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        Router router = Router.router(vertx);

        // Serve the client-side application
        router.route("/").handler(StaticHandler.create());

        // Handle POST requests to calculate hash values
        router.post("/hash").handler(BodyHandler.create());
        router.post("/hash").handler(this::calculateHash);

        // Start the server on port 8080
        vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(8080, result -> {
                if (result.succeeded()) {
                    startFuture.complete();
                } else {
                    startFuture.fail(result.cause());
                }
            });
    }

    private void calculateHash(RoutingContext context) {
        // Extract the input string from the request body
        JsonObject requestBody = context.getBodyAsJson();
        if (requestBody == null || !requestBody.containsKey("input")) {
            context.response().setStatusCode(400).end("Missing input in request body");
            return;
        }

        String input = requestBody.getString("input");
        // Calculate the hash value
        String hashValue = calculateHashValue(input);

        // Respond with the hash value
        JsonObject response = new JsonObject().put("hash", hashValue);
        context.response().end(response.encodePrettily());
    }

    private String calculateHashValue(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to calculate hash value", e);
        }
    }
}
