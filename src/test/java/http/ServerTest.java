package http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aleksandr Shevkunenko on 28.07.2017.
 */
public abstract class ServerTest {
    static protected final int PORT = 1234;
    static protected final String HOST = "localhost";
    static protected final int POOL_SIZE = 10;

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
