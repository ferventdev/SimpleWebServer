package http;

import lombok.val;
import org.junit.jupiter.api.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aleksandr Shevkunenko on 26.07.2017.
 */
class GreetingServerTest {
    public static final int PORT = 1234;

    static Thread serverThread;
//    static ExecutorService clientsPool;

    @BeforeAll
    static void setUp() {
        serverThread = new Thread(() -> Server.main(Integer.toString(PORT)), "server");
//        clientsPool = Executors.newSingleThreadExecutor();
        serverThread.start();
    }

    @AfterAll
    static void tearDown() throws InterruptedException {
//        clientsPool.shutdown();
        serverThread.interrupt();
//        serverThread.join();
    }

    @Test
    void justServerTest() throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
    }

    @Test
    void exchangeTest() throws InterruptedException {
        Request request = Request.from(Request.HttpMethod.GET);
        Thread clientThread = new Thread(new Client("localhost", PORT, request), "client-1");
        clientThread.start();
        TimeUnit.SECONDS.sleep(4);
        clientThread.interrupt();
        clientThread.join();
//        val clientJob = clientsPool.submit(new Client("localhost", PORT, request));
//        clientsPool.execute(new Client("localhost", PORT, request));
//        while (!clientJob.isDone()) {
//            TimeUnit.SECONDS.sleep(1);
//        }
    }
}