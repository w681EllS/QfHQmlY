// 代码生成时间: 2025-08-07 20:47:22
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

// Vert.x 程序入口类，用于内存使用情况分析
public class MemoryUsageAnalyzer extends AbstractVerticle {

    private MemoryMXBean memoryMXBean;

    // 在Verticle初始化时执行
    @Override
    public void init(Vertx vertx, Context context) {
        memoryMXBean = ManagementFactory.getMemoryMXBean();
    }

    // 启动Verticle时执行的操作
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // 注册HTTP服务端点用于获取内存使用情况
        vertx.createHttpServer()
            .requestHandler(request -> {
                request.response().setStatusCode(200).end(getMemoryUsageInfo());
            })
            .listen(8080, http -> {
                if (http.succeeded()) {
                    startPromise.complete();
                } else {
                    startPromise.fail(http.cause());
                }
            });
    }

    // 获取内存使用情况信息
    private String getMemoryUsageInfo() {
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

        JsonObject memoryUsage = new JsonObject()
            .put("heap_used", heapMemoryUsage.getUsed())
            .put("heap_committed", heapMemoryUsage.getCommitted())
            .put("heap_max", heapMemoryUsage.getMax())
            .put("heap_init", heapMemoryUsage.getInit())
            .put("non_heap_used", nonHeapMemoryUsage.getUsed())
            .put("non_heap_committed", nonHeapMemoryUsage.getCommitted())
            .put("non_heap_max", nonHeapMemoryUsage.getMax())
            .put("non_heap_init", nonHeapMemoryUsage.getInit());

        return memoryUsage.encode();
    }

    // 用于启动Vert.x应用程序的主方法
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MemoryUsageAnalyzer(), res -> {
            if (res.succeeded()) {
                System.out.println("Memory usage analyzer is deployed.");
            } else {
                System.err.println("Failed to deploy memory usage analyzer.");
                res.cause().printStackTrace();
            }
        });
    }
}
