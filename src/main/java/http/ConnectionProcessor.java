package http;

import lombok.extern.log4j.Log4j2;

import java.net.Socket;

/**
 * Created by Aleksandr Shevkunenko on 25.07.2017.
 */
@Log4j2
public class ConnectionProcessor implements Runnable {

    private final Socket clientSocket;

    public ConnectionProcessor(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        log.debug(() -> "Client request has been received." + clientSocket);
    }
}
