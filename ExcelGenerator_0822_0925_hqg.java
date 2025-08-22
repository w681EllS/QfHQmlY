// 代码生成时间: 2025-08-22 09:25:47
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

public class ExcelGenerator extends AbstractVerticle {

    // 启动方法
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);
        vertx.createHttpServer()
            .requestHandler(request -> generateExcel(request))
            .listen(config().getInteger("http.port", 8080), result -> {
                if (result.succeeded()) {
                    startFuture.complete();
                } else {
                    startFuture.fail(result.cause());
                }
            });
    }

    // 生成Excel文件方法
    private void generateExcel(RoutingContext routingContext) {
        JsonObject requestData = routingContext.getBodyAsJson();
        String filename = requestData.getString("filename");
        int numberOfSheets = requestData.getInteger("numberOfSheets", 1);
        int rowsPerSheet = requestData.getInteger("rowsPerSheet", 10);

        if (filename == null || filename.isEmpty()) {
            routingContext.response().setStatusCode(400).end("Filename is required");
            return;
        }

        try {
            Workbook workbook = new XSSFWorkbook();
            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.createSheet("Sheet" + (i + 1));
                for (int j = 0; j < rowsPerSheet; j++) {
                    Row row = sheet.createRow(j);
                    for (int k = 0; k < 5; k++) {
                        row.createCell(k).setCellValue("Value" + k);
                    }
                }
            }

            try (FileOutputStream outputStream = new FileOutputStream(filename)) {
                workbook.write(outputStream);
                routingContext.response().setStatusCode(200).end("Excel file generated successfully");
            }
        } catch (IOException e) {
            routingContext.response().setStatusCode(500).end("Error generating Excel file");
        }
    }

    // 主方法
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new ExcelGenerator(), ar -> {
            if (ar.succeeded()) {
                System.out.println("Excel generator started");
            } else {
                System.out.println("Failed to start Excel generator");
            }
        });
    }
}
