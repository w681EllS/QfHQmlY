// 代码生成时间: 2025-09-30 01:33:24
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public class TestScheduler extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        try {
            // 调度任务，每10秒执行一次
            vertx.setPeriodic(10000, id -> {
                executeTask();
            });

            startFuture.complete();
        } catch (Exception e) {
            startFuture.fail(e);
        }
    }

    private void executeTask() {
        // 获取当前时间戳
        long timestamp = LocalDateTime.now(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();
        // 打印当前任务执行的时间戳
        System.out.println("Task executed at: " + timestamp);
    }

    public static void main(String[] args) {
        try {
            // 创建Vertx实例
            Vertx vertx = Vertx.vertx();
            // 部署Verticle
            vertx.deployVerticle(new TestScheduler(), res -> {
                if (res.succeeded()) {
                    System.out.println("TestScheduler deployed successfully");
                } else {
                    System.out.println("Failed to deploy TestScheduler: " + res.cause().getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("Error starting Vertx: " + e.getMessage());
        }
    }
}
