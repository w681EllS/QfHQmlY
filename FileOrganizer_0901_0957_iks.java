// 代码生成时间: 2025-09-01 09:57:24
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class FileOrganizer extends AbstractVerticle {

    private static final String ORGANIZED_FOLDER = "organized";
    private final String rootFolder;

    public FileOrganizer(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        vertx.executeBlocking(promise -> {
            try {
                // Create the organized directory if it does not exist
                File organizedDir = new File(rootFolder, ORGANIZED_FOLDER);
                if (!organizedDir.exists()) {
                    boolean created = organizedDir.mkdirs();
                    if (!created) {
                        throw new IOException("Failed to create the organized folder");
                    }
                }

                // Organize files
                List<File> files = listFilesRecursively(rootFolder);
                for (File file : files) {
                    String relativePath = file.getAbsolutePath().substring(rootFolder.length() + 1);
                    File dest = new File(organizedDir, relativePath);
                    Files.move(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                promise.complete();
            } catch (Exception e) {
                promise.fail(e);
            }
        }, result -> {
            if (result.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(result.cause());
            }
        });
    }

    private List<File> listFilesRecursively(String directoryPath) throws IOException {
        List<File> files = new ArrayList<>();
        Files.walk(Paths.get(directoryPath)).forEach(path -> {
            if (Files.isRegularFile(path)) {
                files.add(path.toFile());
            }
        });
        return files;
    }

    // Main method to start the Vert.x application
    public static void main(String[] args) {
        // Define the root folder to be organized
        String rootFolder = "/path/to/your/folder";
        Vertx vertx = Vertx.vertx();
        ServiceBinder binder = new ServiceBinder(vertx);
        binder.setAddress("file.organizer").register(FileOrganizer.class, new FileOrganizer(rootFolder));
    }
}
