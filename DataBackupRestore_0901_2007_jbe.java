// 代码生成时间: 2025-09-01 20:07:00
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Verticle for handling data backup and restore operations.
 */
public class DataBackupRestore extends AbstractVerticle {
    private static final String BACKUP_FILE_PATH = "backup.zip";
    private static final String DATA_DIRECTORY = "data";

    public static void main(String[] args) {
        // Start the Vert.x application
        io.vertx.core.Vertx vertx = io.vertx.core.Vertx.vertx();
        vertx.deployVerticle(DataBackupRestore.class.getName());
    }

    @Override
    public void start(Future<Void> startFuture) {
        ServiceBinder binder = new ServiceBinder(vertx);
        binder.setAddress("data.backup.restore").register(DataBackupRestoreService.class, this::handleBackupRestore);
        startFuture.complete();
    }

    /**
     * Handles the backup operation.
     * @param message The message containing the backup request.
     * @param resultHandler The handler for the backup result.
     */
    private void handleBackup(JsonObject message, Handler<AsyncResult<JsonObject>> resultHandler) {
        try {
            // Create a new zip output stream for the backup
            OutputStream os = new FileOutputStream(BACKUP_FILE_PATH);
            ZipOutputStream zos = new ZipOutputStream(os);

            // Add all files from the data directory to the zip
            Files.walk(Paths.get(DATA_DIRECTORY))
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            ZipEntry zipEntry = new ZipEntry(file.toString().substring(DATA_DIRECTORY.length() + 1));
                            zos.putNextEntry(zipEntry);
                            Files.copy(file, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

            // Close the zip output stream
            zos.close();
            os.close();

            // Respond with a success message
            resultHandler.handle(Future.succeededFuture(new JsonObject().put("status", "Backup successful")));
        } catch (Exception e) {
            // Handle any errors that occurred during backup
            resultHandler.handle(Future.failedFuture(e.getMessage()));
        }
    }

    /**
     * Handles the restore operation.
     * @param message The message containing the restore request.
     * @param resultHandler The handler for the restore result.
     */
    private void handleRestore(JsonObject message, Handler<AsyncResult<JsonObject>> resultHandler) {
        try {
            // Open the zip input stream for the backup file
            InputStream is = new FileInputStream(BACKUP_FILE_PATH);
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry zipEntry;

            // Extract all entries from the zip to the data directory
            while ((zipEntry = zis.getNextEntry()) != null) {
                String fileName = DATA_DIRECTORY + zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(Paths.get(fileName));
                } else {
                    Files.copy(zis, Paths.get(fileName));
                }
                zis.closeEntry();
            }

            // Close the zip input stream
            zis.close();
            is.close();

            // Respond with a success message
            resultHandler.handle(Future.succeededFuture(new JsonObject().put("status", "Restore successful")));
        } catch (Exception e) {
            // Handle any errors that occurred during restore
            resultHandler.handle(Future.failedFuture(e.getMessage()));
        }
    }

    /**
     * Handles the backup and restore service requests.
     * @param serviceMessage The message containing the service request.
     * @param resultHandler The handler for the service result.
     */
    private void handleBackupRestore(JsonObject serviceMessage, Handler<AsyncResult<JsonObject>> resultHandler) {
        String action = serviceMessage.getString("action");
        if ("backup".equals(action)) {
            handleBackup(serviceMessage, resultHandler);
        } else if ("restore".equals(action)) {
            handleRestore(serviceMessage, resultHandler);
        } else {
            resultHandler.handle(Future.failedFuture("Invalid action"));
        }
    }
}