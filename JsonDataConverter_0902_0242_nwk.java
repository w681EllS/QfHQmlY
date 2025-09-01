// 代码生成时间: 2025-09-02 02:42:26
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import io.vertx.serviceproxy.ProxyUtils;
import io.vertx.serviceproxy.ServiceException;
import java.util.concurrent.TimeoutException;

/**
 * 使用VERTX框架实现的JSON数据格式转换器
 * 此程序接收JSON数据，转换格式后返回
 */
public class JsonDataConverter extends AbstractVerticle {

    // 启动方法
    @Override
    public void start(Future<Void> startFuture) {
        try {
            // 初始化路由器
            Router router = Router.router(vertx);

            // 添加路由处理器
            router.post("/convert").handler(BodyHandler.create());
            router.post("/convert").handler(this::handleJsonConversion);

            // 启动服务
            int port = 8080;
            vertx.createHttpServer()
                .requestHandler(router)
                .listen(port, result -> {
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

    // 处理JSON数据转换的处理器
    private void handleJsonConversion(RoutingContext routingContext) {
        JsonObject requestBody = routingContext.getBodyAsJson();
        if (requestBody == null) {
            routingContext.response().setStatusCode(400).end("Invalid JSON data");
            return;
        }

        try {
            // 格式化JSON数据
            String prettyJson = Json.encodePrettily(requestBody);

            // 返回格式化后的JSON数据
            routingContext.response()
                .putHeader("content-type", "application/json")
                .end(prettyJson);
        } catch (Exception e) {
            // 错误处理
            routingContext.response().setStatusCode(500).end("Error processing request");
        }
    }
}
