// 代码生成时间: 2025-08-16 22:54:19
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * HashCalculator is a Vert.x service that provides hash calculation functionality.
 */
public class HashCalculator extends AbstractVerticle {

    private static final String HASH_SERVICE_ADDRESS = "hash.service";

    @Override
    public void start(Future<Void> startFuture) {
        try {
            // Bind the service to the event bus
            new ServiceBinder(vertx)
                .setAddress(HASH_SERVICE_ADDRESS)
                .register(HashService.class, new HashServiceImpl());

            startFuture.complete();
        } catch (Exception e) {
            startFuture.fail(e);
        }
    }
}

/**
 * HashService is a service interface for hash calculation.
 */
interface HashService {
    String calculateHash(String input);
}

/**
 * HashServiceImpl is the implementation of the HashService.
 */
class HashServiceImpl implements HashService {

    @Override
    public String calculateHash(String input) {
        try {
            // Get the message digest instance for SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Calculate the hash
            byte[] hash = digest.digest(input.getBytes());
            // Encode the hash in Base64
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // Handle the exception if the algorithm is not found
            throw new RuntimeException("Hash algorithm not found", e);
        }
    }
}
