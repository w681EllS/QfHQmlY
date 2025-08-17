// 代码生成时间: 2025-08-18 04:00:46
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;
import io.vertx.ext.web.templ.TemplateEngine;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExcelGenerator extends AbstractVerticle {

    private TemplateEngine templateEngine;

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);

        // Enable CORS
        router.route().handler(CorsHandler.allowAll());

        // Body handler
        router.route().handler(BodyHandler.create());

        // Static handler to serve the frontend
        router.route("/static/*").handler(StaticHandler.create().setCachingEnabled(false));

        // Register routes
        router.post("/generate").handler(this::handleGenerate);

        // Serve the index page
        router.get("/").handler(this::serveIndex);

        // Start the server
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getInteger("http.port", 8080), result -> {
                if (result.succeeded()) {
                    // Initialize Thymeleaf template engine
                    templateEngine = ThymeleafTemplateEngine.create();

                    startPromise.complete();
                } else {
                    startPromise.fail(result.cause());
                }
            });
    }

    private void serveIndex(RoutingContext context) {
        templateEngine.render(context, "index.html", res -> {
            if (res.succeeded()) {
                context.response()
                    .putHeader("content-type", "text/html")
                    .end(res.result());
            } else {
                context.fail(res.cause());
            }
        });
    }

    private void handleGenerate(RoutingContext context) {
        JsonObject requestBody = context.getBodyAsJson();
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Generated Sheet");
            // Create title row
            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("Column 1");
            titleRow.createCell(1).setCellValue("Column 2");
            titleRow.createCell(2).setCellValue("Column 3");

            // Create data rows
            for (int i = 1; i < 11; i++) {
                Row row = sheet.createRow(i);
                row.createCell(0).setCellValue("Data " + i + "-1");
                row.createCell(1).setCellValue("Data " + i + "-2");
                row.createCell(2).setCellValue("Data " + i + "-3");
            }

            // Style the title row
            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.YELLOW.index);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleRow.forEach(cell -> cell.setCellStyle(style));

            // Write data to output stream
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            bos.close();

            // Write to response
            context.response()
                .putHeader("Content-Disposition", "attachment; filename=GeneratedExcel.xlsx")
                .putHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .end(bos.toByteArray());
        } catch (IOException e) {
            context.fail(e);
        }
    }
}
