// 代码生成时间: 2025-08-23 10:14:25
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.FileSystemException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文本文件内容分析器，用于分析文本文件并返回统计信息。
 */
public class TextFileAnalyzer extends AbstractVerticle {

    private static final String ERROR_MSG = "Error occurred while processing file: ";
    private final Map<String, Integer> wordCountMap = new ConcurrentHashMap<>();

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);
        // 注册HTTP服务器以接收文件处理请求
        vertx.createHttpServer()
            .requestHandler(req -> {
                if ("POST".equals(req.method().toString())) {
                    processRequest(req);
                } else {
                    req.response().setStatusCode(405).end("Method Not Allowed");
                }
            })
            .listen(config().getInteger("http.port", 8080), result -> {
                if (result.succeeded()) {
                    startFuture.complete();
                } else {
                    startFuture.fail(result.cause());
                }
            });
    }

    /**
     * 处理HTTP请求，分析上传的文件。
     * @param req HTTP请求对象
     */
    private void processRequest(HttpServerRequest req) {
        req.uploadHandler(upload -> {
            upload.exceptionHandler(err -> {
                req.response().setStatusCode(500).end(ERROR_MSG + err.getMessage());
            });
            upload.endHandler(v -> {
                if (upload.file() != null) {
                    analyzeFile(upload.file());
                } else {
                    req.response().setStatusCode(400).end("No file uploaded");
                }
            });
        });
    }

    /**
     * 分析文件内容并返回统计信息。
     * @param file 上传的文件对象
     */
    private void analyzeFile(JsonObject file) {
        vertx.fileSystem().readFile(file.getString("filename"), ar -> {
            if (ar.succeeded()) {
                String content = new String(ar.result().getBytes(), StandardCharsets.UTF_8);
                wordCountMap.clear(); // 清空之前的计数
                countWords(content);
                JsonObject response = new JsonObject();
                response.put("wordCount", wordCountMap);
                vertx.eventBus().send("analyze_result", response);
            } else {
                vertx.eventBus().send("analyze_result", new JsonObject().put("error", ERROR_MSG + ar.cause().getMessage()));
            }
        });
    }

    /**
     * 统计文本中的单词数量。
     * @param content 文本内容
     */
    private void countWords(String content) {
        String[] words = content.split("[^a-zA-Z]"); // 使用正则表达式分割单词
        for (String word : words) {
            if (!word.isEmpty()) {
                wordCountMap.compute(word.toLowerCase(), (k, v) -> (v == null) ? 1 : v + 1);
            }
        }
    }
}
