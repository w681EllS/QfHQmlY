// 代码生成时间: 2025-08-02 12:31:22
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SortingService extends AbstractVerticle {
    private ServiceBinder binder;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // Bind the service to a specific address, so it can be accessed by other verticles
        binder = new ServiceBinder(vertx)
            .setAddress("sorting.service")
            .register(SortingService.class, this);

        startPromise.complete();
    }

    /*
     * Sorts an array of integers using Collections.sort() method.
     * @param numbers A JsonArray of integers to be sorted.
     * @return A sorted JsonArray of integers.
     */
    public JsonArray sortNumbers(JsonArray numbers) {
        if (numbers == null || numbers.isEmpty()) {
            // Handle the error case where the input array is null or empty
            throw new IllegalArgumentException("Input array cannot be null or empty");
        }

        // Convert the JsonArray to a List for sorting
        List<Integer> numberList = numbers.getList().stream()
            .map(Integer::valueOf)
            .toList();

        // Sort the list using Collections.sort()
        Collections.sort(numberList);

        // Convert the sorted list back to a JsonArray
        return new JsonArray(numberList);
    }

    /*
     * Main method to run the Verticle.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new SortingService(), res -> {
            if (res.succeeded()) {
                System.out.println("Sorting Service started successfully");
            } else {
                System.err.println("Failed to start Sorting Service: " + res.cause().getMessage());
            }
        });
    }
}
