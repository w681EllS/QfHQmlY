// 代码生成时间: 2025-08-03 05:30:31
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;

// HttpServerProcessor 是一个 Vert.x Verticle，用于处理HTTP请求
public class HttpServerProcessor extends AbstractVerticle {

    // 启动方法，当Verticle部署后会被调用
    @Override
    public void start(Future<Void> startFuture) {
        // 创建Http服务器
        HttpServer server = vertx.createHttpServer();

        // 创建一个Router，用于定义路由规则
        Router router = Router.router(vertx);

        // 添加日志处理器
        router.route().handler(LoggerHandler.create());

        // 添加静态文件服务
        router.route("/static/*").handler(StaticHandler.create("webroot"));

        // 添加请求体处理器，以便接收JSON数据
        router.route().handler(BodyHandler.create().setUploadsDirectory("uploads"));

        // 定义一个简单的路由，返回一个字符串
        router.get("/hello").handler(this::handleHello);

        // 启动HTTP服务器，监听8080端口
        server.requestHandler(router).listen(8080, result -> {
            if (result.succeeded()) {
                startFuture.complete();
                System.out.println("HTTP server started on port 8080");
            } else {
                startFuture.fail(result.cause());
                System.out.println("Failed to start HTTP server");
            }
        });
    }

    // 处理 /hello 路径的请求
    private void handleHello(RoutingContext context) {
        try {
            // 获取请求参数
            String name = context.request().getParam("name");
            if (name == null) {
                // 如果没有提供'name'参数，则使用默认值'World'
                name = "World";
            }

            // 响应请求
            context.response()
                .putHeader("content-type", "text/plain")
                .end("Hello, " + name + "!");
        } catch (Exception e) {
            // 错误处理
            context.response().setStatusCode(500).end("Internal Server Error");
        }
    }
}
