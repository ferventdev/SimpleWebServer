package http;

import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.junit.jupiter.api.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by Aleksandr Shevkunenko on 26.07.2017.
 */
@Log4j2
class GreetingServerTest {
    public static final int PORT = 1234;

    static Thread serverThread;
    static ExecutorService clientsPool;

    @BeforeAll
    static void setUp() {
        serverThread = new Thread(() -> Server.main(Integer.toString(PORT)), "server");
        clientsPool = Executors.newSingleThreadExecutor();
        serverThread.start();
    }

    @AfterAll
    static void tearDown() throws InterruptedException {
        clientsPool.shutdown();
        clientsPool.awaitTermination(3000, TimeUnit.MILLISECONDS);
        serverThread.interrupt();
//        serverThread.join();
    }

    @Test
    void justServerTest() throws InterruptedException, UnknownHostException {
        TimeUnit.SECONDS.sleep(3);
        log.info(InetAddress.getLocalHost().toString());
        log.info(InetAddress.getLocalHost().getCanonicalHostName());
        log.info(InetAddress.getLocalHost().getHostAddress());
        log.info(InetAddress.getLocalHost().getHostName());
    }

    @Test
    void exchangeTest() throws InterruptedException, ExecutionException {
        Request request = Request.from(Request.HttpMethod.GET);
        Future<String> clientJob = clientsPool.submit(new Client("localhost", PORT, request));
        assertThat(clientJob.get(), is(String.format(ConnectionProcessor.RESPONSE_HEADER, GreetingServer.GREET.length(), GreetingServer.GREET)));
    }
}