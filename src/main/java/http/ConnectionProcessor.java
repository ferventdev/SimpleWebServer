package http;

import java.net.Socket;

/**
 * Created by Aleksandr Shevkunenko on 25.07.2017.
 */
public class ConnectionProcessor implements Runnable {

    private final Socket clientSocket;

    public ConnectionProcessor(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {

    }
}
