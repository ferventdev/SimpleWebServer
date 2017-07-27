package http;

import http.Request.HttpMethod;
import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
    private static final int DEFAULT_HTTP_PORT = 80;
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

            String[] terms = line.split("\\s+");
            if (terms.length < 3) {
                log.debug(() -> String.format("Connection %d: the client request first line is invalid.", id));
                return null;
            }

            HttpMethod method = HttpMethod.valueOf(terms[0].toUpperCase());
            if (method != HttpMethod.GET) {
                log.debug(() -> String.format("Connection %d: currently only GET method is supported.", id));
                return null;
            }

            String path = terms[1];
            String httpVersion = terms[2];

            val headers = readHeaders();
            if (headers.isEmpty()) {
                log.debug(() -> String.format("Connection %d: the client request contains no headers.", id));
                return null;
            }
            if (!headers.containsKey("Host")) {
                log.debug(() -> String.format("Connection %d: the client request contains no \"Host\" header.", id));
                return null;
            }

            String[] hostAndPort = headers.get("Host").split("\\s*:\\s*", 2);
            int port = -1;
            try {
                port = (hostAndPort.length == 1) ? DEFAULT_HTTP_PORT : Integer.parseInt(hostAndPort[1]);
                if (port <= 0 || port > 65535) {
                    log.debug(() -> String.format("Connection %d: port number is outside the valid range in the client request.", id));
                    port = DEFAULT_HTTP_PORT;
                }
            } catch (NumberFormatException e) {
                log.debug(() -> String.format("Connection %d: port number isn't a parsable integer in the client request.", id));
                port = DEFAULT_HTTP_PORT;
            }

            URL url = new URL("http", hostAndPort[0], port, path);

            val params = getParameters(url.getQuery());

            val body = new StringBuilder();
            while (reader.ready() && (line = reader.readLine()) != null) body.append(line);

            return Request.from(method, url.getPath(), httpVersion, params, url.getHost(), url.getPort(), headers, body.toString());

        } catch (IllegalArgumentException e) {
            log.error(() -> String.format("Connection %d: method in the client request doesn't exist in the HTTP specification.", id));
            return null;
        } catch (IOException e) {
            log.error(() -> String.format("Connection %d: an IO error occurred when reading the client request.", id));
            return null;
        }
    }

    private Map<String, String> getParameters(String query) {
        Map<String, String> params = new HashMap<>();
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length > 1) params.put(pair[0], pair[1]);
            else log.debug(() -> String.format("Connection %d: parameter '%s' has no value in the client request path query.", id, pair[0]));
        }
        return params;
    }

    private Map<String, String> readHeaders() throws IOException {
        Map<String, String> headers = new HashMap<>();
        for (String s = null; (s = reader.readLine()) != null && !s.trim().isEmpty(); ) {
            String[] header = s.split("\\s*:\\s*", 2);
            if (header.length > 1) headers.put(header[0], header[1]);
            else log.debug(() -> String.format("Connection %d: header '%s' has no value in the client request.", id, header[0]));
        }
        return headers;
    }
}
