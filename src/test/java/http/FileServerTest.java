package http;

import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Aleksandr Shevkunenko on 28.07.2017.
 */
class FileServerTest extends ServerTest {

    @BeforeAll
    static void setUp() {
        serverThread = new Thread(() -> Server.main(Integer.toString(PORT)), "server");
        serverThread.start();
    }

}