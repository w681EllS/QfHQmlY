// 代码生成时间: 2025-08-09 02:02:01
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import java.util.Random;

// TestDataGeneratorVerticle 是一个 Vert.x 组件，用于生成测试数据
public class TestDataGeneratorVerticle extends AbstractVerticle {

    // 启动方法
    @Override
    public void start() throws Exception {
        // 注册服务代理
        ServiceBinder binder = new ServiceBinder(vertx);
        binder.setAddress("test.data.generator").register(TestDataGeneratorService.class, new TestDataGeneratorServiceImpl());
    }
}

// TestDataGeneratorService 是一个服务接口
public interface TestDataGeneratorService {
    // 生成测试数据的方法
    String generateTestData(int count);
}

// TestDataGeneratorServiceImpl 是服务接口的实现类
public class TestDataGeneratorServiceImpl implements TestDataGeneratorService {

    // 随机数生成器
    private Random random = new Random();

    // 实现接口方法，生成测试数据
    @Override
    public String generateTestData(int count) {
        // 检查输入数量是否有效
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than zero");
        }

        // 创建 JSON 数组来存储测试数据
        JsonArray testData = new JsonArray();

        // 生成指定数量的测试数据
        for (int i = 0; i < count; i++) {
            // 随机生成测试数据对象
            JsonObject dataObject = new JsonObject();
            dataObject.put("id", i);
            dataObject.put("name", "Test User " + i);
            dataObject.put("age", 18 + random.nextInt(50)); // 18到68岁之间
            dataObject.put("email", "test.user" + i + "@example.com");
            testData.add(dataObject);
        }

        // 返回生成的测试数据 JSON 字符串
        return testData.toString();
    }
}
