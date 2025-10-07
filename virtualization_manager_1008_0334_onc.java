// 代码生成时间: 2025-10-08 03:34:27
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceProxyBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VirtualizationManager extends AbstractVerticle {

    // A map to simulate VM database
    private Map<String, JsonObject> vmDatabase;

    public VirtualizationManager() {
        vmDatabase = new HashMap<>();
    }

    @Override
    public void start(Future<Void> startFuture) {
        // Bind a service proxy to a specific address
        ServiceBinder binder = new ServiceBinder(vertx);
        binder.setAddress("virtualization.service").register(VirtualizationService.class, this::handleMessage);
        startFuture.complete();
    }

    // Handle the service message
    private void handleMessage(JsonObject message) {
        try {
            String action = message.getString("action");
            switch (action) {
                case "create":
                    createVM(message);
                    break;
                case "start":
                    startVM(message);
                    break;
                case "stop":
                    stopVM(message);
                    break;
                case "list":
                    listVMs();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown action: " + action);
            }
        } catch (Exception e) {
            // Handle exceptions and send an error response
            JsonObject errorResponse = new JsonObject().put("error", e.getMessage());
            // Send the error response back to the client
            // vertx.eventBus().send(message.getString("replyAddress"), errorResponse);
        }
    }

    // Create a new VM
    private void createVM(JsonObject message) throws Exception {
        String vmName = message.getString("vmName");
        if (vmName == null || vmName.isEmpty()) {
            throw new IllegalArgumentException("VM name must be provided");
        }

        String vmId = UUID.randomUUID().toString();
        vmDatabase.put(vmId, new JsonObject().put("name", vmName).put("status", "stopped"));
        JsonObject response = new JsonObject().put("vmId", vmId).put("status", "VM created successfully");
        // Send the response back to the client
        // vertx.eventBus().send(message.getString("replyAddress"), response);
    }

    // Start a VM
    private void startVM(JsonObject message) throws Exception {
        String vmId = message.getString("vmId");
        JsonObject vm = vmDatabase.get(vmId);
        if (vm == null) {
            throw new IllegalArgumentException("VM not found");
        }
        vm.put("status", "running");
        JsonObject response = new JsonObject().put("status", "VM started successfully");
        // Send the response back to the client
        // vertx.eventBus().send(message.getString("replyAddress"), response);
    }

    // Stop a VM
    private void stopVM(JsonObject message) throws Exception {
        String vmId = message.getString("vmId");
        JsonObject vm = vmDatabase.get(vmId);
        if (vm == null) {
            throw new IllegalArgumentException("VM not found");
        }
        vm.put("status", "stopped");
        JsonObject response = new JsonObject().put("status", "VM stopped successfully");
        // Send the response back to the client
        // vertx.eventBus().send(message.getString("replyAddress"), response);
    }

    // List all VMs
    private void listVMs() throws Exception {
        JsonObject response = new JsonObject().put("vms", new JsonObject(vmDatabase));
        // Send the response back to the client
        // vertx.eventBus().send(message.getString("replyAddress"), response);
    }
}
