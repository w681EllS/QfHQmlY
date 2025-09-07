// 代码生成时间: 2025-09-07 09:24:41
package com.example.urlvalidator;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;

public class UrlValidatorService extends AbstractVerticle {

    private HttpClient client;

    @Override
    public void start(Future<Void> startFuture) {
        // Configure the HttpClient options
        HttpClientOptions options = new HttpClientOptions()
            .setTryUseCompression(true)
            .setLogActivity(true);

        // Create an HttpClient instance
        client = vertx.createHttpClient(options);
        // Indicate that the verticle is ready to start
        startFuture.complete();
    }

    // Method to validate URL link
    public void validateUrl(String url, Promise<Boolean> result) {
        try {
            // Check if the URL is not null or empty
            if (url == null || url.trim().isEmpty()) {
                result.fail("Invalid URL provided");
                return;
            }

            // Make a HEAD request to the URL
            HttpClientRequest request = client.head(url, response -> {
                if (response.statusCode() < 400) {
                    result.complete(true);
                } else {
                    result.complete(false);
                }
            });

            // Set a timeout for the request
            request.setTimeout(5000);

            // Handle request exceptions
            request.exceptionHandler(err -> {
                result.fail("Error while making request to the URL");
            });

            // End the request (necessary to send it)
            request.end();
        } catch (Exception e) {
            result.fail("An error occurred while validating the URL: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        // Close the HttpClient when the verticle is stopped
        client.close();
    }
}
