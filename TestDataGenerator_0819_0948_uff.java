// 代码生成时间: 2025-08-19 09:48:14
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.serviceproxy.ServiceException;
import java.util.Random;

public class TestDataGenerator extends AbstractVerticle {
    private static final String SERVICE_NAME = "TestDataGenerator";
    private static final int PORT = 8080;
    private static final int MAX_DATA_SET_SIZE = 100;

    @Override
    public void start(Future<Void> startFuture) {
        Router router = Router.router(vertx);

        // Handle JSON body
        router.route().handler(BodyHandler.create().setUp());

        // Serve static resources
        router.route("/").handler(StaticHandler.create());

        // Generate test data endpoint
        router.get("/data").handler(this::generateTestData);

        // Start the HTTP server
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(PORT, result -> {
                if (result.succeeded()) {
                    startFuture.complete();
                } else {
                    startFuture.fail(result.cause());
                }
            });
    }

    /**
     * Handles the /data endpoint by generating random test data.
     *
     * @param context Routing context of the HTTP request.
     */
    private void generateTestData(RoutingContext context) {
        try {
            int dataSetSize = getRandomInt(1, MAX_DATA_SET_SIZE);
            JsonArray testData = generateRandomJsonArray(dataSetSize);
            context.response()
                .putHeader("content-type", "application/json")
                .end(testData.encode());
        } catch (Exception e) {
            // Handle errors and send back a 500 Internal Server Error.
            context.response().setStatusCode(500).end("Internal Server Error");
        }
    }

    /**
     * Generates a random integer between min and max.
     *
     * @param min Minimum value.
     * @param max Maximum value.
     * @return A random integer between min and max.
     */
    private int getRandomInt(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    /**
     * Generates a random JSON array of specified size.
     *
     * @param size The size of the JSON array.
     * @return A JSON array with random data.
     */
    private JsonArray generateRandomJsonArray(int size) {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < size; i++) {
            JsonObject jsonObject = new JsonObject().
                put("id", getRandomInt(1, 1000)).
                put("name", "Item " + getRandomInt(1, 100)).
                put("price", getRandomDouble(0.0, 100.0)).
                put("quantity", getRandomInt(1, 100));
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /**
     * Generates a random double between min and max.
     *
     * @param min Minimum value.
     * @param max Maximum value.
     * @return A random double between min and max.
     */
    private double getRandomDouble(double min, double max) {
        Random random = new Random();
        return min + (max - min) * random.nextDouble();
    }
}
