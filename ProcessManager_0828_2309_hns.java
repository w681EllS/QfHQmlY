// 代码生成时间: 2025-08-28 23:09:28
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ProcessManager service provides functionality to manage processes.
 * It allows starting, stopping, and listing processes.
 */
public class ProcessManager extends AbstractVerticle {

    private List<Process> managedProcesses = new CopyOnWriteArrayList<>();

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
        // Bind the service to the event bus with a unique address
        ServiceBinder binder = new ServiceBinder(vertx);
        binder
            .setAddress("process.manager")
            .register(ProcessManagerService.class, this::startProcess, this::stopProcess, this::listProcesses);
    }

    /**
     * Handles a request to start a process.
     * @param command The command to execute.
     * @param resultHandler The handler for the result of the process start operation.
     */
    public void startProcess(String command, io.vertx.core.Handler<AsyncResult<Void>> resultHandler) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            Process process = processBuilder.start();
            managedProcesses.add(process);
            resultHandler.handle(io.vertx.core.Future.succeededFuture());
        } catch (Exception e) {
            resultHandler.handle(io.vertx.core.Future.failedFuture("Failed to start process: " + e.getMessage()));
        }
    }

    /**
     * Handles a request to stop a process.
     * @param pid The process ID to stop.
     * @param resultHandler The handler for the result of the process stop operation.
     */
    public void stopProcess(int pid, io.vertx.core.Handler<AsyncResult<Void>> resultHandler) {
        Process toDestroy = null;
        for (Process process : managedProcesses) {
            if (process.pid() == pid) {
                toDestroy = process;
                break;
            }
        }
        if (toDestroy != null) {
            toDestroy.destroy();
            managedProcesses.remove(toDestroy);
            resultHandler.handle(io.vertx.core.Future.succeededFuture());
        } else {
            resultHandler.handle(io.vertx.core.Future.failedFuture("Process with PID " + pid + " not found"));
        }
    }

    /**
     * Handles a request to list all managed processes.
     * @param resultHandler The handler for the result of the process list operation.
     */
    public void listProcesses(io.vertx.core.Handler<AsyncResult<List<Process>>> resultHandler) {
        resultHandler.handle(io.vertx.core.Future.succeededFuture(new CopyOnWriteArrayList<>(managedProcesses)));
    }
}
