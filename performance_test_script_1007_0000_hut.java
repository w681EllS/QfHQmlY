// 代码生成时间: 2025-10-07 00:00:17
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

public class PerformanceTestScript extends AbstractVerticle {

    private NetServer server;
    private NetClient client;
    private int port = 8080; // Port number for the server to listen on

    @Override
    public void start() throws Exception {
        // Start the server
        server = vertx.createNetServer();
        server.connectHandler(this::handleServerConnection);
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("Server is listening on port: \" + port + \"");
            } else {
                System.err.println("Failed to start the server: \" + result.cause().getMessage() + \"");
           }
        });

        // Start the client
        client = vertx.createNetClient();
    }

    private void handleServerConnection(NetSocket socket) {
        socket.handler(buffer -> {
            // Echo the received data back to the client
            socket.write(buffer);
        });
    }

    public static void main(String[] args) {
        // Set up Vert.x options
        VertxOptions options = new VertxOptions().setWorkerPoolSize(20);
        // Create the Vert.x instance
        Vertx vertx = Vertx.vertx(options);
        // Deploy the verticle
        vertx.deployVerticle(new PerformanceTestScript(), new DeploymentOptions().setInstances(4), result -> {
            if (result.succeeded()) {
                System.out.println("Performance test script deployed successfully!");
            } else {
                System.err.println("Failed to deploy performance test script: \" + result.cause().getMessage() + \"");
            }
        });
    }
}
