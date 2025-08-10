// 代码生成时间: 2025-08-10 08:57:03
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * MathCalculatorVerticle is the verticle root handling all math
 * operations requests.
 */
public class MathCalculatorVerticle extends AbstractVerticle {

    private MathService service;
    private ServiceBinder binder;

    @Override
    public void start(Future<Void> startFuture) {
        service = new MathServiceImpl();
        binder = new ServiceBinder(vertx);
        binder.setAddress(MathService.SERVICE_ADDRESS)
            .register(MathService.class, service);
        startFuture.complete();
    }
}

/**
 * MathService is the service interface for math operations.
 */
interface MathService {
    String SERVICE_ADDRESS = "math.service";

    void add(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler);
    void subtract(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler);
    void multiply(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler);
    void divide(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler);
}

/**
 * MathServiceImpl is the implementation of the MathService interface.
 */
class MathServiceImpl implements MathService {

    @Override
    public void add(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler) {
        try {
            resultHandler.handle(Future.succeededFuture(new JsonObject().put("result", a + b)));
        } catch (Exception e) {
            resultHandler.handle(Future.failedFuture(new JsonObject().put("error", e.getMessage())));
        }
    }

    @Override
    public void subtract(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler) {
        try {
            resultHandler.handle(Future.succeededFuture(new JsonObject().put("result", a - b)));
        } catch (Exception e) {
            resultHandler.handle(Future.failedFuture(new JsonObject().put("error", e.getMessage())));
        }
    }

    @Override
    public void multiply(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler) {
        try {
            resultHandler.handle(Future.succeededFuture(new JsonObject().put("result", a * b)));
        } catch (Exception e) {
            resultHandler.handle(Future.failedFuture(new JsonObject().put("error", e.getMessage())));
        }
    }

    @Override
    public void divide(int a, int b, Handler<AsyncResult<JsonObject>> resultHandler) {
        try {
            if(b == 0) {
                resultHandler.handle(Future.failedFuture(new JsonObject().put("error", "Cannot divide by zero")));
            } else {
                resultHandler.handle(Future.succeededFuture(new JsonObject().put("result", a / b)));
            }
        } catch (Exception e) {
            resultHandler.handle(Future.failedFuture(new JsonObject().put("error", e.getMessage())));
        }
    }
}
