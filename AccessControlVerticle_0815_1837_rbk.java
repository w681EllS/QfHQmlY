// 代码生成时间: 2025-08-15 18:37:43
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.UsernamePasswordAuthentication;
import io.vertx.ext.auth.shiro.ShiroAuthRealm;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;

public class AccessControlVerticle extends AbstractVerticle {

    private Router router;
    private ShiroAuth shiroAuth;

    @Override
    public void start(Future<Void> startFuture) {
        // Initialize the event bus
        EventBus eventBus = vertx.eventBus();

        // Initialize Shiro authentication
        initShiroAuth();

        // Create a router object
        router = Router.router(vertx);

        // Create a session handler
        SessionHandler sessionHandler = SessionHandler.create(LocalSessionStore.create(vertx));

        // Add body handler to router for JSON processing
        router.route().handler(BodyHandler.create().setUploadsDirectory("").setDeleteUploadedFilesOnEnd(true));

        // Define routes and handlers
        router.post("/login").handler(this::loginHandler);
        router.get("/protected").handler(SessionHandler.sessionHandler());
        router.get("/protected").handler(this::protectedRouteHandler);

        // Start listening on port 8080
        vertx.createHttpServer()
          .requestHandler(router)
          .listen(8080, result -> {
              if (result.succeeded()) {
                  startFuture.complete();
              } else {
                  startFuture.fail(result.cause());
              }
          });
    }

    private void initShiroAuth() {
        // Configure Shiro authentication
        JsonObject config = new JsonObject();
        config.put("type", ShiroAuthRealmType.JDBC);
        config.put("config", new JsonObject().put("sql", "SELECT username, password FROM users WHERE username = ?"));
        config.put("dataSource", vertx.getOrCreateContext().config().getJsonObject("db"));

        // Create a ShiroAuth object
        ShiroAuthRealm authRealm = ShiroAuthRealm.create(vertx, config);
        authRealm.getAuthorizationManager()
          .addRole("admin", new JsonObject().put("principals", new JsonObject().put("admin", new JsonObject()));

        // Store the ShiroAuth instance
        shiroAuth = ShiroAuth.create(vertx, authRealm);
    }

    private void loginHandler(RoutingContext context) {
        JsonObject credentials = context.getBodyAsJson();
        UsernamePasswordAuthentication authInfo = new UsernamePasswordAuthentication(
          credentials.getString("username"),
          credentials.getString("password")
        );

        shiroAuth.authenticate(authInfo, res -> {
            if (res.succeeded()) {
                // Authentication succeeded
                User user = res.result();
                context.setUser(user);
                context.session().put("userId", user.principal().getString("userId"));
                context.response().setStatusCode(200).end("Logged in successfully");
            } else {
                // Authentication failed
                context.response().setStatusCode(401).end("Authentication failed");
            }
        });
    }

    private void protectedRouteHandler(RoutingContext context) {
        // Check if the user is logged in
        if (context.user() != null) {
            // User is logged in, proceed to the next handler
            context.next();
        } else {
            // User is not logged in, return 401 Unauthorized
            context.response().setStatusCode(401).end("Not authorized");
        }
    }
}
