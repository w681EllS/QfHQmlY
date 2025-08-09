// 代码生成时间: 2025-08-10 00:49:59
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.common.template.handlebars.HandlebarsTemplateEngine;
import io.vertx.ext.web.handler.TemplateHandler;
import java.util.Collections;

public class ResponsiveLayoutVertxApp extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        try {
            // 创建一个Router对象，用于定义路由配置
            Router router = Router.router(vertx);

            // 配置静态文件服务，用于提供前端资源
            router.route("/*").handler(StaticHandler.create("webroot"));

            // 创建Handlebars模板引擎
            TemplateEngine templateEngine = HandlebarsTemplateEngine.create(vertx);

            // 配置模板处理器，用于渲染响应式布局页面
            router.get("/").handler(TemplateHandler.create(templateEngine, "index.hbs"));

            // 配置错误处理路由
            router.route("/*").last().handler(this::handleNotFound);

            // 创建HTTP服务器并监听8080端口
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

    // 错误处理方法，用于处理未找到的路由
    private void handleNotFound(RoutingContext context) {
        context.response().setStatusCode(404).end("Not Found");
    }
}
