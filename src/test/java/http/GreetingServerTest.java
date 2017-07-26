package http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Aleksandr Shevkunenko on 26.07.2017.
 */
class GreetingServerTest {
    public static final int PORT = 1234;

    Thread serverThread;
    ExecutorService clientsPool;

    @BeforeEach
    void setUp() {
        clientsPool = Executors.newSingleThreadExecutor();
        serverThread = new Thread(() -> Server.main(Integer.toString(PORT)), "Server");
    }

    @AfterEach
    void tearDown() {
        clientsPool.shutdown();
        serverThread.interrupt();
    }

    @Test
    void exchangeTest() {
        Request request = Request.from(Request.HttpMethod.GET);
        clientsPool.execute(new Client("localhost", PORT, request));
    }
}