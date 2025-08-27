// 代码生成时间: 2025-08-27 11:36:26
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.parsetools.JsonEvent;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.core.streams.ReadStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;

public class FileBackupSyncTool extends AbstractVerticle {

    private static final String SOURCE_DIR = "/path/to/source";
    private static final String BACKUP_DIR = "/path/to/backup";
    private ConcurrentHashMap<String, Long> lastModifiedMap = new ConcurrentHashMap<>();

    @Override
    public void start(Promise<Void> startPromise) {
        vertx.executeBlocking(promise -> {
            try {
                backupAndSyncFiles();
                promise.complete();
            } catch (Exception e) {
                promise.fail(e);
            }
        }, res -> {
            if (res.succeeded()) {
                startPromise.complete();
            } else {
                startPromise.fail(res.cause());
            }
        });
    }

    private void backupAndSyncFiles() throws IOException {
        File sourceDir = new File(SOURCE_DIR);
        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File file : files) {
                String sourcePath = file.getAbsolutePath();
                String backupPath = BACKUP_DIR + sourcePath.substring(SOURCE_DIR.length());
                File backupFile = new File(backupPath);

                if (file.isFile()) {
                    backupFile(file, backupFile);
                } else if (file.isDirectory()) {
                    ensureDirectoryExists(backupFile);
                    backupAndSyncFilesRecursive(file, backupFile);
                }
            }
        }
    }

    private void backupAndSyncFilesRecursive(File sourceDir, File backupDir) throws IOException {
        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File file : files) {
                String sourcePath = file.getAbsolutePath();
                String backupPath = backupDir.getAbsolutePath() + File.separator + file.getName();
                File backupFile = new File(backupPath);

                if (file.isFile()) {
                    backupFile(file, backupFile);
                } else if (file.isDirectory()) {
                    ensureDirectoryExists(backupFile);
                    backupAndSyncFilesRecursive(file, backupFile);
                }
            }
        }
    }

    private void backupFile(File sourceFile, File backupFile) throws IOException {
        long lastModified = sourceFile.lastModified();
        if (!backupFile.exists() || lastModifiedMap.getOrDefault(sourceFile.getAbsolutePath(), 0L) < lastModified) {
            Files.copy(sourceFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            lastModifiedMap.put(sourceFile.getAbsolutePath(), lastModified);
        }
    }

    private void ensureDirectoryExists(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
        }
    }
}
