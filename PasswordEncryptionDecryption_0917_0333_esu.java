// 代码生成时间: 2025-09-17 03:33:04
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.RoutingContext;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordEncryptionDecryption extends AbstractVerticle {

    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 128;
    private static final String CHARSET = "UTF-8";

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        Router router = Router.router(vertx);

        // Handle POST request to encrypt password
        router.post("/encrypt").handler(BodyHandler.create());
        router.post("/encrypt").handler(this::handleEncrypt);

        // Handle POST request to decrypt password
        router.post("/decrypt").handler(BodyHandler.create());
        router.post("/decrypt").handler(this::handleDecrypt);

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getInteger("http.port", 8080), result -> {
                if (result.succeeded()) {
                    startPromise.complete();
                } else {
                    startPromise.fail(result.cause());
                }
            });
    }

    private void handleEncrypt(RoutingContext context) {
        String password = context.getBodyAsJson().getString("password");
        try {
            SecretKey key = generateKey();
            String encrypted = encrypt(password, key);
            context.response().setStatusCode(200).end(new JsonObject().put("encrypted", encrypted).encode());
        } catch (Exception e) {
            context.response().setStatusCode(500).end("Error encrypting password");
        }
    }

    private void handleDecrypt(RoutingContext context) {
        String encryptedPassword = context.getBodyAsJson().getString("encryptedPassword");
        try {
            SecretKey key = generateKey();
            String decrypted = decrypt(encryptedPassword, key);
            context.response().setStatusCode(200).end(new JsonObject().put("decrypted", decrypted).encode());
        } catch (Exception e) {
            context.response().setStatusCode(500).end("Error decrypting password");
        }
    }

    // Generate a random secret key
    private SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE, SecureRandom.getInstanceStrong());
        return keyGenerator.generateKey();
    }

    // Encrypt the password using AES
    private String encrypt(String password, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(password.getBytes(CHARSET));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // Decrypt the password using AES
    private String decrypt(String encryptedPassword, SecretKey key) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(encryptedPassword);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(decoded), CHARSET);
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new PasswordEncryptionDecryption());
    }
}
