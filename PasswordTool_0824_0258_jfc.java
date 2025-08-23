// 代码生成时间: 2025-08-24 02:58:52
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordTool extends AbstractVerticle {

    private static final String ALGORITHM = "AES";
    private static final String CHARSET_NAME = "UTF-8";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String RSA_ALGORITHM = "RSA";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private SecretKey secretKey;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        ServiceProxyBuilder serviceProxyBuilder = new ServiceProxyBuilder(vertx);
        vertx.deployVerticle(PasswordTool.class.getName(), res -> {
            if (res.succeeded()) {
                serviceProxyBuilder.setAddress("password.tool").build(PasswordService.class, passwordService -> {
                    // Client code can interact with the service here
                });
            }
        });
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(128, SecureRandom.getInstanceStrong());
        secretKey = keyGenerator.generateKey();
        startFuture.complete();
    }

    public JsonObject encrypt(String password) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(password.getBytes(CHARSET_NAME));
        String encryptedPassword = Base64.getEncoder().encodeToString(encryptedBytes);
        return new JsonObject().put("encryptedPassword", encryptedPassword);
    }

    public JsonObject decrypt(String encryptedPassword) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedPassword);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        String decryptedPassword = new String(decryptedBytes, CHARSET_NAME);
        return new JsonObject().put("decryptedPassword", decryptedPassword);
    }
}
