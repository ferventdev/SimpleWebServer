package http;

/**
 * Created by Aleksandr Shevkunenko on 25.07.2017.
 */
public class Client implements Runnable {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static int count = 1;

    private String host;
    private int port;
    private int id;


    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        this.id = count++;
    }

    public Client(String host) {
        this(host, DEFAULT_PORT);
    }

    public Client() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }


    @Override
    public void run() {

    }
}
