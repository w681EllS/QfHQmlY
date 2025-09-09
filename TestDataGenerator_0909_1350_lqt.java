// 代码生成时间: 2025-09-09 13:50:19
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * 测试数据生成器实现类
 *
 * 提供生成测试数据的服务
 */
public class TestDataGenerator extends AbstractVerticle {

    private ServiceBinder binder;

    @Override
    public void start(Promise<Void> startPromise) {
        try {
            // 绑定服务代理
            binder = new ServiceBinder(vertx);
            binder.setAddress("test.data.generator")
                .register(TestDataGeneratorService.class, new TestDataGeneratorServiceImpl());

            startPromise.complete();
        } catch (Exception e) {
            startPromise.fail(e);
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        binder.unregister();
    }
}

/**
 * 测试数据生成器服务接口
 */
interface TestDataGeneratorService {

    /**
     * 生成测试数据
     *
     * @param count 要生成的数据条数
     * @return 返回生成的测试数据数组
     */
    String generateTestData(int count);
}

/**
 * 测试数据生成器服务实现类
 */
class TestDataGeneratorServiceImpl implements TestDataGeneratorService {

    @Override
    public String generateTestData(int count) {
        JsonArray data = new JsonArray();
        for (int i = 0; i < count; i++) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put("id", i)
                       .put("name", "Test User " + i)
                       .put("email", "testuser" + i + "@example.com");
            data.add(jsonObject);
        }
        return data.encodePrettily();
    }
}
