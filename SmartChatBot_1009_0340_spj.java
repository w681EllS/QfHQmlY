// 代码生成时间: 2025-10-09 03:40:22
package com.example.chatbot;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import io.vertx.serviceproxy.ServiceProxy;

public class SmartChatBot extends AbstractVerticle {

    private ServiceProxy botServiceProxy;

    @Override
    public void start(Future<Void> startFuture) {
        try {
            botServiceProxy = new ServiceProxyBuilder(vertx)
                    .setAddress("chatbot.service")
                    .build(SmartChatBotService.class);

            Router router = Router.router(vertx);
            router.route().handler(BodyHandler.create());

            // Serve UI files from the webroot directory
            router.route("/*").handler(StaticHandler.create());

            // Handle chatbot requests
            router.post("/chatbot").handler(this::handleChatbotRequest);

            vertx.createHttpServer()
                .requestHandler(router)
                .listen(config().getInteger("http.port", 8080), result -> {
                    if (result.succeeded()) {
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });

        } catch (Exception e) {
            startFuture.fail(e);
        }
    }

    private void handleChatbotRequest(RoutingContext context) {
        JsonObject request = context.getBodyAsJson();
        if (request == null) {
            context.response().setStatusCode(400).end("Bad Request");
            return;
        }

        String message = request.getString("message");
        if (message == null) {
            context.response().setStatusCode(400).end("Message is required");
            return;
        }

        botServiceProxy.mapToService(request, res -> {
            if (res.succeeded()) {
                JsonObject response = res.result();
                context.response().setStatusCode(200).end(response.encodePrettily());
            } else {
                context.response().setStatusCode(500).end("Internal Server Error");
            }
        });
    }
}
