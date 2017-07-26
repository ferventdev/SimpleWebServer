package http;

import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.io.*;
import java.net.Socket;

/**
 * Created by Aleksandr Shevkunenko on 25.07.2017.
 */
@Log4j2
public abstract class ConnectionProcessor implements Runnable {

    private static String HTTP_CHARSET = "8859_1";
    private static int count = 1;

    private final Socket clientSocket;
    protected PrintWriter writer;
    private final int id;

    public ConnectionProcessor(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.id = count++;
        log.debug(() -> String.format("Client request has been received and connection %d has been created.", id));
    }

    @Override
    public void run() {
        try (val in = clientSocket.getInputStream();
             val reader = new BufferedReader(new InputStreamReader(in, HTTP_CHARSET)) ) {

            writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), HTTP_CHARSET), false);
            log.debug(() -> String.format("Connection %d: the server successfully connected to the client (both IO streams created).", id));

            send(getResponse(getRequest()));

            log.debug(() -> String.format("Connection %d has been completely processed.", id));

        } catch (UnsupportedEncodingException e) {
            log.error(() -> String.format("Connection %d: client socket IO streams were not created due to the wrong encoding parameter.", id));
        } catch (IOException e) {
            log.error(() -> String.format("Connection %d: an IO error occurred when creating the client socket IO streams.", id));
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (Throwable t) { /* do nothing*/ }
            log.debug(() -> String.format("Connection %d has been closed (both IO streams closed).", id));
        }
    }

    protected abstract Response getResponse(Request request);

    protected abstract void send(Response response);

    private Request getRequest() {
        return Request.from(Request.HttpMethod.GET);
    }
}
