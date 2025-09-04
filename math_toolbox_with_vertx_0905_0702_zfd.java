// 代码生成时间: 2025-09-05 07:02:44
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;

// 定义一个接口来表示数学工具集的服务
public interface MathToolboxService {
    String ADD = "math:add";
    String SUBTRACT = "math:subtract";
    String MULTIPLY = "math:multiply";
    String DIVIDE = "math:divide";

    void add(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler);
    void subtract(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler);
    void multiply(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler);
    void divide(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler);
}

// 实现接口的服务类
public class MathToolboxServiceImpl implements MathToolboxService {
    @Override
    public void add(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(new JsonObject().put("result", a + b)));
    }

    @Override
    public void subtract(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(new JsonObject().put("result", a - b)));
    }

    @Override
    public void multiply(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(new JsonObject().put("result", a * b)));
    }

    @Override
    public void divide(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler) {
        if (b != 0) {
            resultHandler.handle(Future.succeededFuture(new JsonObject().put("result", a / b)));
        } else {
            resultHandler.handle(Future.failedFuture("Cannot divide by zero"));
        }
    }
}

// Verticle类，启动和注册服务
public class MathToolboxVerticle extends AbstractVerticle {
    @Override
    public void start(Future<Void> startFuture) {
        // 绑定服务到event bus
        ServiceBinder binder = new ServiceBinder(vertx);
        binder
            .setAddress(MathToolboxService.ADD)
            .register(MathToolboxService.class, MathToolboxServiceImpl::new);
        binder
            .setAddress(MathToolboxService.SUBTRACT)
            .register(MathToolboxService.class, MathToolboxServiceImpl::new);
        binder
            .setAddress(MathToolboxService.MULTIPLY)
            .register(MathToolboxService.class, MathToolboxServiceImpl::new);
        binder
            .setAddress(MathToolboxService.DIVIDE)
            .register(MathToolboxService.class, MathToolboxServiceImpl::new);

        startFuture.complete();
    }
}

// 使用注解来声明服务接口
@ProxyGen
public interface MathToolboxProxy extends MathToolboxService {

}

// 服务代理的实现
public class MathToolboxProxyImpl extends AbstractVerticle implements MathToolboxProxy {
    private final MathToolboxService service;

    public MathToolboxProxyImpl(MathToolboxService service) {
        this.service = service;
    }

    @Override
    public void add(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler) {
        service.add(a, b, resultHandler);
    }

    @Override
    public void subtract(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler) {
        service.subtract(a, b, resultHandler);
    }

    @Override
    public void multiply(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler) {
        service.multiply(a, b, resultHandler);
    }

    @Override
    public void divide(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler) {
        service.divide(a, b, resultHandler);
    }
}

// 客户端使用代理的例子
public class MathToolboxClient {
    private final MathToolboxProxy proxy;

    public MathToolboxClient(MathToolboxProxy proxy) {
        this.proxy = proxy;
    }

    public void calculateAdd(int a, int b) {
        proxy.add(a, b, res -> {
            if (res.succeeded()) {
                System.out.println("Addition result: " + res.result().getJsonObject("result").getInteger("result"));
            } else {
                res.cause().printStackTrace();
            }
        });
    }

    // 其他计算方法...
}