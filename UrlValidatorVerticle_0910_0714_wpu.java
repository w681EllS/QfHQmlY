// 代码生成时间: 2025-09-10 07:14:17
 * It provides a simple REST API endpoint to check the URL status.
 */
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

public class UrlValidatorVerticle extends AbstractVerticle {

    private HttpClient httpClient;

    @Override
    public void start(Future<Void> startFuture) {
        httpClient = vertx.createHttpClient(new HttpClientOptions()
            .setSsl(true)
            .setTrustAll(true)); // For local development, trust all certificates

        HttpServerOptions serverOptions = new HttpServerOptions();
        HttpServer server = vertx.createHttpServer(serverOptions);
        Router router = Router.router(vertx);

        // Static files for the frontend
        router.route("/").handler(StaticHandler.create().setCachingEnabled(false));

        // Socket connection
        BridgeOptions bridgeOptions = new BridgeOptions().addInboundPermitted(new PermittedOptions().setAddress("urlValidation"));
        SockJSHandlerOptions sockJSHandlerOptions = new SockJSHandlerOptions().setHeartbeatInterval(2000);
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, sockJSHandlerOptions).bridge(bridgeOptions);
        router.route("/eventbus/*").handler(sockJSHandler);

        // POST endpoint to validate URLs
        router.post("/validate-url").handler(this::validateUrlHandler);

        server.requestHandler(router).listen(config().getInteger("http.port", 8080), result -> {
            if (result.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(result.cause());
            }
        });
    }

    private void validateUrlHandler(RoutingContext routingContext) {
        String url = routingContext.getBodyAsJson().getString("url");
        if (url == null || url.trim().isEmpty()) {
            routingContext.response().setStatusCode(400).end("URL is required.");
            return;
        }

        httpClient.head(url, response -> {
            if (response.succeeded()) {
                int statusCode = response.result().statusCode();
                if (statusCode == 200) {
                    routingContext.response().end(new JsonObject().put("isValid", true).put("message", "URL is valid").toString());
                } else {
                    routingContext.response().end(new JsonObject().put("isValid", false).put("message", "URL is not valid").put("statusCode", statusCode).toString());
                }
            } else {
                routingContext.response().end(new JsonObject().put("isValid", false).put("message", "Error checking URL").toString());
            }
        });
    }
}
