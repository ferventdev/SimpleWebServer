package http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aleksandr Shevkunenko on 28.07.2017.
 */
public interface ServerTest {
    int PORT = 1234;
    String HOST = "localhost";
    int POOL_SIZE = 10;

    Thread serverThread = new Thread(() -> Server.main(Integer.toString(PORT)), "server");;
    ExecutorService clientsPool = Executors.newFixedThreadPool(POOL_SIZE); ;

    @BeforeAll
    static void setUp() {
        serverThread.start();
    }

    @AfterAll
    static void tearDown() throws InterruptedException {
        clientsPool.shutdown();
        clientsPool.awaitTermination(3000, TimeUnit.MILLISECONDS);
        clientsPool.shutdownNow();
        serverThread.interrupt();
    }
}
