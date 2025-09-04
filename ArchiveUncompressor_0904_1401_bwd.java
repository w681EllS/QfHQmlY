// 代码生成时间: 2025-09-04 14:01:33
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
# 增强安全性
import io.vertx.ext.web.RoutingContext;
# TODO: 优化性能
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ArchiveUncompressor extends AbstractVerticle {

    private static final String ARCHIVE_UPLOAD_ROUTE = "/archive/upload";
    private static final String EXTRACTION_DIRECTORY = "uploads";
    private static final String BRIDGE_EVENTBUS_ADDRESS = "archive.uncompress";
    private static final String BRIDGE_PERMITTED = "permit";
# 改进用户体验

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        // Handle file uploads
        router.post(ARCHIVE_UPLOAD_ROUTE).handler(this::handleFileUpload);

        // Serve the static resources
        router.route("/static/*").handler(StaticHandler.create());

        // Setup EventBus bridge
        SockJSHandlerOptions options = new SockJSHandlerOptions().setheartbeatInterval(5000);
        BridgeOptions bridgeOptions = new BridgeOptions().addOutboundPermitted(new PermittedOptions().setAddress(BRIDGE_EVENTBUS_ADDRESS));
        SockJSHandler ebHandler = SockJSHandler.create(vertx, options).bridge(bridgeOptions);
        router.route("/eventbus/*").handler(ebHandler);

        vertx.createHttpServer().requestHandler(router).listen(config().getInteger("http.port", 8080));
    }

    private void handleFileUpload(RoutingContext context) {
        String filename = context.request().getParam("filename");
        context.request().bodyHandler(buffer -> {
            try {
                Path outputPath = Paths.get(EXTRACTION_DIRECTORY, filename);
                Files.createDirectories(outputPath.getParent());
                extractArchive(buffer.getBytes(), outputPath);
# 改进用户体验

                context.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("message", "Archive uncompressed successfully").toString());
            } catch (IOException | ArchiveException e) {
                context.response().setStatusCode(500).end(new JsonObject().put("error", e.getMessage()).toString());
            }
        });
# FIXME: 处理边界情况
    }

    private void extractArchive(byte[] archiveData, Path outputPath) throws IOException, ArchiveException {
        try (TarArchiveInputStream tarInput = new TarArchiveInputStream(
                new GzipCompressorInputStream(new ByteArrayInputStream(archiveData)))) {
            TarArchiveEntry entry;
# TODO: 优化性能
            while ((entry = tarInput.getNextTarEntry()) != null) {
# 添加错误处理
                Path entryPath = outputPath.resolve(entry.getName());
# 改进用户体验
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.copy(tarInput, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
# 添加错误处理
}
