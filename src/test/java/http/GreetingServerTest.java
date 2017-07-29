package http;

import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.StringReader;
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
class GreetingServerTest implements ServerTest {

    private static String simpleRequest = String.format("GET / HTTP/1.1\r\n" +
                    "Host: %s:%d\r\n" +
                    "Connection: keep-alive\r\n" +
                    "Pragma: no-cache\r\n" +
                    "Cache-Control: no-cache\r\n" +
                    "Upgrade-Insecure-Requests: 1\r\n" +
                    "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 YaBrowser/17.6.1.745 Yowser/2.5 Safari/537.36\r\n" +
                    "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\n" +
                    "DNT: 1\r\n" +
                    "Accept-Encoding: gzip, deflate, sdch, br\r\n" +
                    "Accept-Language: ru,en;q=0.8\r\n" +
                    "X-Compress: null\r\n\r\n", HOST, PORT);
    public static final String RESPONSE_HEADER = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html\r\n" +
//                    "Content-Language: en, ru\r\n" +
                    "Content-Length: %d\r\n" +
                    "Connection: close\r\n\r\n%s";


    @Test
    void justServerTest() throws InterruptedException, UnknownHostException {
        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    void oneRequestTest() throws Exception {
        requestsFromClientsTestHelper(1);
    }

    @Test
    void manyRequestsTest() throws Exception {
        requestsFromClientsTestHelper(5);
    }

    private void requestsFromClientsTestHelper(int numOfClients) throws Exception {
        Request request = ConnectionProcessor.getRequest(new BufferedReader(new StringReader(simpleRequest)), -1);

        List<Callable<String>> clientsJobs = new ArrayList<>();
        for (int i = 0; i < numOfClients; i++) {
            Client client = new Client(HOST, PORT, request);
            clientsJobs.add(client);
        }

        val serverResponses = clientsPool.invokeAll(clientsJobs);

        for (val sr : serverResponses) {
            assertThat(sr.get(), is(String.format(RESPONSE_HEADER, GreetingServer.GREET.length(), GreetingServer.GREET)));
        }
    }
}