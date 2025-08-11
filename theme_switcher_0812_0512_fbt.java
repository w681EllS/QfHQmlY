// 代码生成时间: 2025-08-12 05:12:36
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ProxyHelper;

// Service interface for theme switching
public interface ThemeSwitcherService {
    String ADDRESS = "theme.switcher.service";

    void switchTheme(String themeName, Future<JsonObject> resultHandler);
}

// Service implementation
public class ThemeSwitcherServiceImpl implements ThemeSwitcherService {

    private JsonObject userPreferences;

    public ThemeSwitcherServiceImpl(JsonObject userPreferences) {
        this.userPreferences = userPreferences;
    }

    @Override
    public void switchTheme(String themeName, Future<JsonObject> resultHandler) {
        try {
            // Validating themeName
            if (themeName == null || themeName.isEmpty()) {
                resultHandler.fail("Theme name cannot be null or empty");
                return;
            }

            // Simulating theme switch (in a real scenario, you might save this preference)
            userPreferences.put("theme", themeName);
            resultHandler.complete(userPreferences);
        } catch (Exception e) {
            resultHandler.fail(e.getMessage());
        }
    }
}

// Verticle to deploy the service
public class ThemeSwitcherVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        // Deploy Service
        new ServiceBinder(vertx)
            .setAddress(ThemeSwitcherService.ADDRESS)
            .register(ThemeSwitcherService.class, new ThemeSwitcherServiceImpl(new JsonObject().put("theme", "default")));

        startFuture.complete();
    }
}

// Util class to generate Proxy
public class ThemeSwitcherServiceUtil {
    private static final String SERVICE_ADDRESS = ThemeSwitcherService.ADDRESS;

    public static ThemeSwitcherService getThemeSwitcherService(io.vertx.core.Vertx vertx) {
        return ProxyHelper.createProxy(ThemeSwitcherService.class, vertx, SERVICE_ADDRESS);
    }
}
