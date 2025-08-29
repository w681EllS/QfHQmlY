// 代码生成时间: 2025-08-29 08:10:03
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.common.template.HandlebarsTemplateEngine;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.cors.Cors;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.FileUploadHandler;
import io.vertx.ext.web.FileUploadHandlerType;
import io.vertx.ext.web.templ.HandlebarsTemplateEngine;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.auth.impl.jose.JWT;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestHandler extends AbstractVerticle {

    private HttpServer server;
    private Router router;
    private TemplateEngine engine;
    private OAuth2Auth oauth2Provider;
    private LocalSessionStore sessionStore;
    private SessionHandler sessionHandler;

    @Override
    public void start(Future<Void> startFuture) {
        router = Router.router(vertx);
        engine = HandlebarsTemplateEngine.create(vertx);

        // 设置静态文件服务
        Router staticRouter = Router.router(vertx);
        staticRouter.route().handler(StaticHandler.create().setCachingEnabled(false));
        router.mountSubRouter("/static", staticRouter);

        // 添加日志处理器
        router.route().handler(LoggerHandler.create());

        // 添加跨域处理器
        router.route().handler(CorsHandler.create(Cors.create("*").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST).allowedMethod(HttpMethod.OPTIONS)));

        // 处理文件上传
        router.route("/file-upload").handler(FileUploadHandler.create().setUploadsDirectory("uploads/"));

        // 添加Body处理器
        router.route().handler(BodyHandler.create());

        // 添加Session处理器
        sessionStore = LocalSessionStore.create(vertx);
        sessionHandler = SessionHandler.create(sessionStore);
        router.route().handler(sessionHandler);

        // 添加OAuth2处理器
        oauth2Provider = OAuth2Auth.create(vertx, config());
        router.route("/login").handler(this::handleLogin);
        router.route("/callback").handler(this::handleCallback);
        router.route("/logout").handler(this::handleLogout);

        // 添加错误处理器
        router.route("/*").failureHandler(this::handleFailure);

        // 启动HTTP服务
        server = vertx.createHttpServer()
                .requestHandler(router)
                .listen(config().getInteger("http.port", 8080), result -> {
                    if (result.succeeded()) {
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });
    }

    private void handleLogin(RoutingContext context) {
        // 获取请求参数
        String username = context.request().getParam("username");
        String password = context.request().getParam("password");
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);

        // 进行认证
        oauth2Provider.authenticate(credentials, res -> {
            if (res.succeeded()) {
                User user = res.result();
                // 处理成功登录
                context.response().setStatusCode(200).end("Login Successful");
            } else {
                // 处理失败登录
                context.response().setStatusCode(401).end("Login Failed");
            }
        });
    }

    private void handleCallback(RoutingContext context) {
        // 获取Code
        String code = context.request().getParam("code");
        // 通过Code获取Access Token
        oauth2Provider.authenticateCode(code, res -> {
            if (res.succeeded()) {
                JWT jwt = res.result();
                // 处理成功获取Access Token
                context.response().setStatusCode(200).end("Token Retrieved");
            } else {
                // 处理失败获取Access Token
                context.response().setStatusCode(401).end("Token Retrieval Failed");
            }
        });
    }

    private void handleLogout(RoutingContext context) {
        // 清除Session
        context.session().destroy();
        // 返回成功响应
        context.response().setStatusCode(200).end("Logout Successful");
    }

    private void handleFailure(RoutingContext context) {
        // 处理错误
        int statusCode = context.statusCode();
        if(statusCode == 404){
            context.response().setStatusCode(404).end("Not Found");
        } else {
            context.response().setStatusCode(500).end("Internal Server Error");
        }
    }

    @Override
    public void stop() throws Exception {
        if (server != null) {
            server.close();
        }
    }
}
