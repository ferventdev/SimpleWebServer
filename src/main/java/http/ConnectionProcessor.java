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

    public static final String RESPONSE_HEADER =
                    "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html\r\n" +
//                    "Content-Language: en, ru\r\n" +
                    "Content-Length: %d\r\n" +
                    "Connection: close\r\n\r\n%s";

    private static String HTTP_CHARSET = "utf-8"; //"8859_1";
    private static int count = 1;

    private final Socket clientSocket;
    protected BufferedReader reader;
    protected PrintWriter writer;
    private final int id;

    public ConnectionProcessor(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.id = count++;
        log.debug(() -> String.format("Client request has been received and connection %d has been created.", id));
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), HTTP_CHARSET));
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
            try {
                if (reader != null) reader.close();
            } catch (Throwable t) { /* do nothing*/ }
            log.debug(() -> String.format("Connection %d has been closed (both IO streams closed).", id));
        }
    }

    protected abstract Response getResponse(Request request);

    protected void send(Response response) {
        writer.print(RESPONSE_HEADER);
    }

    private Request getRequest() {
        try {
            String line = reader.readLine().trim();
            if (line == null) {
                log.debug(() -> String.format("Connection %d: the client request is empty.", id));
                return null;
            }

        } catch (IOException e) {
            log.error(() -> String.format("Connection %d: an IO error occurred when reading the client request.", id));
        }
        return null;
    }
}
