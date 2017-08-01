package http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This abstract class is used in tests. It holds some basic parameters fields.
 */
public abstract class ServerTest {
    static protected final int PORT = 1234;
    static protected final String HOST = "localhost";
    static protected final int POOL_SIZE = 8;

    static protected final ExecutorService clientsPool = Executors.newFixedThreadPool(POOL_SIZE);
    static protected Thread serverThread;

    @AfterAll
    static void tearDown() throws InterruptedException {
        clientsPool.shutdown();
        clientsPool.awaitTermination(3000, TimeUnit.MILLISECONDS);
        clientsPool.shutdownNow();
        serverThread.interrupt();
    }
}
