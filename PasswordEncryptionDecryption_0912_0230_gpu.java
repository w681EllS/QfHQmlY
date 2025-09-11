// 代码生成时间: 2025-09-12 02:30:58
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordEncryptionDecryption extends AbstractVerticle {

    // Generates a new AES key for encryption and decryption
    private static SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    // Encrypts the given password using AES algorithm
    public static String encrypt(String password, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(password.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Decrypts the given password using AES algorithm
    public static String decrypt(String encryptedPassword, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedPassword);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    @Override
    public void start(Future<Void> startFuture) {
        try {
            // Generate a new AES key for encryption and decryption
            SecretKey key = generateAESKey();

            // Initialize the Vert.x event bus handler
            vertx.eventBus().consumer("encrypt", message -> {
                String password = message.body().toString();
                try {
                    String encryptedPassword = encrypt(password, key);
                    message.reply(new JsonObject().put("encrypted", encryptedPassword));
                } catch (Exception e) {
                    message.fail(400, "Encryption failed");
                }
            });

            // Initialize the Vert.x event bus handler for decryption
            vertx.eventBus().consumer("decrypt", message -> {
                String encryptedPassword = message.body().toString();
                try {
                    String decryptedPassword = decrypt(encryptedPassword, key);
                    message.reply(new JsonObject().put("decrypted", decryptedPassword));
                } catch (Exception e) {
                    message.fail(400, "Decryption failed");
                }
            });

            startFuture.complete();
        } catch (Exception e) {
            startFuture.fail(e);
        }
    }

    // Main method to run the Verticle
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new PasswordEncryptionDecryption(), res -> {
            if (res.succeeded()) {
                System.out.println("PasswordEncryptionDecryption Verticle deployed successfully");
            } else {
                System.err.println("Failed to deploy PasswordEncryptionDecryption Verticle");
            }
        });
    }
}
