// 代码生成时间: 2025-09-14 17:29:28
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;

import java.util.concurrent.TimeUnit;

// 定义进程管理器的服务接口
public interface ProcessManagerService {
    // 启动进程
    void startProcess(String command, Handler<AsyncResult<JsonObject>> resultHandler);
    // 停止进程
    void stopProcess(String processId, Handler<AsyncResult<JsonObject>> resultHandler);
}

// 进程管理器服务的实现类
public class ProcessManagerServiceImpl implements ProcessManagerService {
    private Vertx vertx;
    private Map<String, Process> processMap;

    public ProcessManagerServiceImpl(Vertx vertx) {
        this.vertx = vertx;
        this.processMap = new ConcurrentHashMap<>();
    }

    @Override
    public void startProcess(String command, Handler<AsyncResult<JsonObject>> resultHandler) {
        try {
            // 创建新进程
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            Process process = processBuilder.start();

            // 获取进程ID
            String processId = String.valueOf(process.pid());
            processMap.put(processId, process);

            // 返回进程启动结果
            resultHandler.handle(Future.succeededFuture(new JsonObject().put("processId", processId)));
        } catch (Exception e) {
            // 处理异常
            resultHandler.handle(Future.failedFuture(e));
        }
    }

    @Override
    public void stopProcess(String processId, Handler<AsyncResult<JsonObject>> resultHandler) {
        try {
            // 获取并停止进程
            Process process = processMap.get(processId);
            if (process != null) {
                process.destroy();
                process.waitFor(5, TimeUnit.SECONDS);
                processMap.remove(processId);
                resultHandler.handle(Future.succeededFuture(new JsonObject().put("message", "Process stopped successfully")));
            } else {
                resultHandler.handle(Future.failedFuture(new JsonObject().put("message", "Process not found")));
            }
        } catch (Exception e) {
            // 处理异常
            resultHandler.handle(Future.failedFuture(e));
        }
    }
}

// 进程管理器的Verticle
public class ProcessManagerVerticle extends AbstractVerticle {
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        // 创建服务实例
        ProcessManagerService service = new ProcessManagerServiceImpl(vertx);

        // 绑定服务
        ServiceBinder binder = new ServiceBinder(vertx);
        binder.setAddress("process.manager")
            .register(ProcessManagerService.class, service);

        // 启动服务
        startFuture.complete();
    }
}
