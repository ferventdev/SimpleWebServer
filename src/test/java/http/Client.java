package http;

import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Created by Aleksandr Shevkunenko on 25.07.2017.
 */
@Log4j2
public class Client implements Callable<String> {

    private static String HTTP_CHARSET = "utf-8"; //"8859_1";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static int count = 1;

    private final String host;
    private final int port;
    private final int id;
    private final Request request;


    public Client(String host, int port, Request request) {
        this.host = host;
        this.port = port;
        this.id = count++;
        this.request = request;
        log.debug(() -> String.format("Client %d created.", id));
    }

    public Client(String host, Request request) {
        this(host, DEFAULT_PORT, request);
    }

    public Client(Request request) {
        this(DEFAULT_HOST, DEFAULT_PORT, request);
    }


    @Override
    public String call() {
        log.debug(() -> String.format("Client %d launched.", id));

        String response = null;
        try (val socket = new Socket(host, port);
             val out = socket.getOutputStream();
             val writer = new PrintWriter(new OutputStreamWriter(out, HTTP_CHARSET), false);
             val in = socket.getInputStream();
             val reader = new BufferedReader(new InputStreamReader(in, HTTP_CHARSET))) {

            log.debug(() -> String.format("Client %d successfully connected to the server.", id));

            response = reader.lines().collect(Collectors.joining("\r\n"));//new StringBuilder();

            String finalResponse = response;
            log.info(() -> String.format("Client %d has got a response from the server:%n%s", id, finalResponse));

//            } catch (IOException e) {
//                log.error(() -> String.format("Client %d: an IO error occurred while getting response from the server.", id));
//            }

            log.debug(() -> String.format("Client %d completed his work.", id));

        } catch (UnknownHostException e) {
            log.error(() -> String.format("Client %d could'n connect to the server, because its IP address couldn't be determined.", id));
        } catch (IllegalArgumentException e) {
            log.error(() -> String.format("Client %d could'n connect to the server, because the port number is outside the valid range.", id));
        } catch (UnsupportedEncodingException e) {
            log.error(() -> String.format("Client %d connected to the server, but the socket's IO streams were not created due to the wrong encoding parameter.", id));
        } catch (IOException e) {
            log.error(() -> String.format("Client %d: an IO error occurred when the socket or any of its IO streams was created.", id));
            log.error(e.getMessage());
        } finally {
            log.debug(() -> String.format("Client %d disconnected from the server.", id));
        }
        return response;
    }
}
