// 代码生成时间: 2025-09-13 00:21:13
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import java.util.logging.Logger;

public class DocumentConverter extends AbstractVerticle {
    private static final Logger logger = Logger.getLogger(DocumentConverter.class.getName());

    // Define the address for service proxy
    private static final String DOCUMENT_CONVERTER_ADDRESS = "document.converter.service";

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // Initialize the router
        Router router = Router.router(vertx);

        // Handle static files (for UI)
        router.route("/*").handler(StaticHandler.create());

        // Handle document conversion requests
        router.post("/convert").handler(this::handleConvertRequest);

        // Create HTTP server and start listening
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getInteger("http.port", 8080), result -> {
                if (result.succeeded()) {
                    logger.info("HTTP server started on port 8080");
                    startPromise.complete();
                } else {
                    startPromise.fail(result.cause());
                }
            });
    }

    private void handleConvertRequest(RoutingContext context) {
        // Parse the request body as JSON
        JsonObject requestBody = context.getBodyAsJson();
        if (requestBody == null) {
            context.fail(400, new IllegalArgumentException("Request body is missing or not valid JSON"));
            return;
        }

        // Extract file and format info from the request body
        String sourceFormat = requestBody.getString("sourceFormat");
        String targetFormat = requestBody.getString("targetFormat");
        String documentContent = requestBody.getString("documentContent");

        if (sourceFormat == null || targetFormat == null || documentContent == null) {
            context.fail(400, new IllegalArgumentException("Missing required parameters in request body"));
            return;
        }

        // Convert document using the document conversion service
        DocumentConverterService service = new ServiceProxyBuilder(vertx)
            .setAddress(DOCUMENT_CONVERTER_ADDRESS)
            .build(DocumentConverterService.class);
        service.convertDocument(documentContent, sourceFormat, targetFormat, result -> {
            if (result.succeeded()) {
                String convertedDocument = result.result();
                context.response()
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("convertedDocument", convertedDocument).toBuffer());
            } else {
                context.fail(500, result.cause());
            }
        });
    }
}

/**
 * DocumentConverterService.java
 * Service interface for document conversion.
 */
package com.example.services;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

@VertxGen
@ProxyGen
public interface DocumentConverterService {
    /**
     * Convert a document from one format to another.
     *
     * @param documentContent The content of the document to convert.
     * @param sourceFormat The original format of the document.
     * @param targetFormat The desired format of the document.
     * @param resultHandler A handler to be called with the conversion result.
     */
    void convertDocument(String documentContent, String sourceFormat, String targetFormat, Handler<AsyncResult<String>> resultHandler);

    /**
     * Create a service proxy for the document converter.
     */
    @ProxyClose
    void close();
}