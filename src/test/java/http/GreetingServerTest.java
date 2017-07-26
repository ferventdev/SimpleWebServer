package http;

import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.junit.jupiter.api.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
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
    static int numberOfClients = 5;

    @BeforeAll
    static void setUp() {
        serverThread = new Thread(() -> Server.main(Integer.toString(PORT)), "server");
        clientsPool = Executors.newFixedThreadPool(numberOfClients); //newSingleThreadExecutor();
        serverThread.start();
    }

    @AfterAll
    static void tearDown() throws InterruptedException {
        clientsPool.shutdown();
        clientsPool.awaitTermination(3000, TimeUnit.MILLISECONDS);
        clientsPool.shutdownNow();
        serverThread.interrupt();
    }

    @Test
    void justServerTest() throws InterruptedException, UnknownHostException {
        TimeUnit.SECONDS.sleep(3);
    }

    @Test
    void oneClientExchangeTest() throws InterruptedException, ExecutionException {
        Request request = Request.from(Request.HttpMethod.GET);
        Future<String> serverResponse = clientsPool.submit(new Client("localhost", PORT, request));
        assertThat(serverResponse.get(), is(String.format(ConnectionProcessor.RESPONSE_HEADER, GreetingServer.GREET.length(), GreetingServer.GREET)));
    }

    @Test
    void manyClientsExchangeTest() throws InterruptedException, ExecutionException {
        Request request = Request.from(Request.HttpMethod.GET);

        List<Callable<String>> clientsJobs = new ArrayList<>();
        for (int i = 0; i < numberOfClients; i++) {
            clientsJobs.add(new Client("localhost", PORT, request));
        }

        val serverResponses = clientsPool.invokeAll(clientsJobs);

        for (val sr : serverResponses) {
            assertThat(sr.get(), is(String.format(ConnectionProcessor.RESPONSE_HEADER, GreetingServer.GREET.length(), GreetingServer.GREET)));
        }
    }
}