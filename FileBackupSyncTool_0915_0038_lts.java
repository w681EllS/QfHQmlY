// 代码生成时间: 2025-09-15 00:38:03
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.FileSystemException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileBackupSyncTool extends AbstractVerticle {

    private static final String SOURCE_DIR = "/path/to/source/directory";
    private static final String BACKUP_DIR = "/path/to/backup/directory";
    private final FileSystem vertxFileSystem = vertx.fileSystem();

    @Override
    public void start(Promise<Void> startPromise) {
        vertx.deployVerticle(this, res -> {
            if (res.succeeded()) {
                startPromise.complete();
            } else {
                startPromise.fail(res.cause());
            }
        });
    }

    public void backupFiles() {
        vertx.executeBlocking(promise -> {
            try {
                Files.walk(Paths.get(SOURCE_DIR))
                    .filter(Files::isRegularFile)
                    .forEach(sourcePath -> {
                        Path targetPath = Paths.get(BACKUP_DIR, sourcePath.toString().substring(SOURCE_DIR.length()));
                        Files.createDirectories(targetPath.getParent());
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    });
                promise.complete();
            } catch (IOException e) {
                promise.fail(e);
            }
        }, res -> {
            if (res.succeeded()) {
                System.out.println("Backup completed successfully.");
            } else {
                System.out.println("Backup failed: " + res.cause().getMessage());
            }
        });
    }

    public void syncFiles() {
        vertx.executeBlocking(promise -> {
            try {
                Files.walk(Paths.get(SOURCE_DIR))
                    .filter(Files::isRegularFile)
                    .forEach(sourcePath -> {
                        Path targetPath = Paths.get(BACKUP_DIR, sourcePath.toString().substring(SOURCE_DIR.length()));
                        if (Files.exists(targetPath) && Files.getLastModifiedTime(sourcePath)
                            .toMillis() > Files.getLastModifiedTime(targetPath).toMillis()) {
                            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    });
                promise.complete();
            } catch (IOException e) {
                promise.fail(e);
            }
        }, res -> {
            if (res.succeeded()) {
                System.out.println("Sync completed successfully.");
            } else {
                System.out.println("Sync failed: " + res.cause().getMessage());
            }
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new FileBackupSyncTool(), res -> {
            if (res.succeeded()) {
                System.out.println("Verticle deployed successfully.");
            } else {
                System.out.println("Failed to deploy verticle: " + res.cause().getMessage());
            }
        });
    }
}
