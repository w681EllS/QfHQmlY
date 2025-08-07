// 代码生成时间: 2025-08-07 08:51:47
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BulkFileRenamer extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        vertx.executeBlocking(promise -> {
            try {
                // 获取文件列表
                List<String> files = listFiles(config().getString("directory"));
                // 重命名文件
                renameFiles(files, config().getString("prefix"));
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

    /**
     * 获取指定目录下的所有文件名
     *
     * @param directory 目录路径
     * @return 文件名列表
     */
    private List<String> listFiles(String directory) throws IOException {
        Path path = Paths.get(directory);
        try (Stream<Path> stream = Files.walk(path)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }

    /**
     * 重命名文件
     *
     * @param files 文件名列表
     * @param prefix 新文件名前缀
     */
    private void renameFiles(List<String> files, String prefix) {
        for (String file : files) {
            String oldPath = file;
            String newName = prefix + file;
            String newPath = file.substring(0, file.lastIndexOf('/') + 1) + newName;
            vertx.fileSystem().rename(oldPath, newPath, res -> {
                if (res.succeeded()) {
                    System.out.println("Renamed: " + oldPath + " to " + newPath);
                } else {
                    System.err.println("Failed to rename: " + oldPath + " to " + newPath);
                }
            });
        }
    }

    public static void main(String[] args) {
        // 配置文件重命名工具
        JsonObject config = new JsonObject().put("directory", "/path/to/files").put("prefix", "new_");
        // 部署Verticle
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new BulkFileRenamer(), res -> {
            if (res.succeeded()) {
                System.out.println("BulkFileRenamer deployed successfully");
            } else {
                System.err.println("Failed to deploy BulkFileRenamer: " + res.cause());
            }
        });
    }
}
