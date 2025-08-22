// 代码生成时间: 2025-08-22 17:05:15
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;

import java.net.URI;
import java.net.http.HttpClientBuilder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UrlValidatorApp extends AbstractVerticle {

    private HttpClient httpClient;

    @Override
    public void start(Future<Void> startFuture) {
        HttpClientOptions options = new HttpClientOptions();
        httpClient = vertx.createHttpClient(options);
        // 启动HTTP服务器
        setUpHttpServer();
        startFuture.complete();
    }

    private void setUpHttpServer() {
        vertx.createHttpServer()
            .requestHandler(request -> {
                try {
                    String urlToCheck = request.getParam("url");
                    if (urlToCheck == null || urlToCheck.isEmpty()) {
                        request.response().setStatusCode(400).end("URL parameter is missing");
                        return;
                    }
                    validateUrl(urlToCheck).onComplete(isValid -> {
                        if (isValid.succeeded()) {
                            boolean isUrlValid = isValid.result();
                            request.response().setStatusCode(isUrlValid ? 200 : 404).end();
                        } else {
                            request.response().setStatusCode(500).end("Error checking URL");
                        }
                    });
                } catch (Exception e) {
                    request.response().setStatusCode(500).end("Error processing request: " + e.getMessage());
                }
            }).listen(8080, http -> {
                if (http.succeeded()) {
                    System.out.println("Server is running on port 8080");
                } else {
                    System.out.println("Server start failed: " + http.cause().getMessage());
                }
            });
    }

    // 验证URL是否有效
    private void validateUrl(String url, Promise<Boolean> promise) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpClientBuilder builder = HttpClientBuilder.create();
        try (var client = builder.build()) {
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() < 400) {
                        promise.complete(true);
                    } else {
                        promise.complete(false);
                    }
                })
                .exceptionally(throwable -> {
                    promise.fail(throwable);
                    return null;
                });
        }
    }

    public static void main(String[] args) {
        Vertx vertx =_vertx;
        UrlValidatorApp urlValidatorApp = new UrlValidatorApp();
        vertx.deployVerticle(urlValidatorApp);
    }
}
