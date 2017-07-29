package http;

import http.Request.HttpMethod;
import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aleksandr Shevkunenko on 25.07.2017.
 */
@Log4j2
public abstract class ConnectionProcessor implements Runnable {

    protected static String HTTP_CHARSET = "utf-8";
    private static final int DEFAULT_HTTP_PORT = 80;
    private static int count = 1;

    private final Socket clientSocket;
    protected OutputStream writer;
    protected final int cpId;

    public ConnectionProcessor(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.cpId = count++;
        log.debug(() -> String.format("Client connection request has been received and connection %d has been created.", cpId));
    }

    @Override
    public void run() {
        try (val reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), HTTP_CHARSET))) {

            writer = new BufferedOutputStream(clientSocket.getOutputStream());

            log.debug(() -> String.format("Connection %d: the server successfully connected to the client (both IO streams created).", cpId));

            send(getResponse(getRequest(reader, cpId)));

            log.debug(() -> String.format("Connection %d has been completely processed.", cpId));

        } catch (UnsupportedEncodingException e) {
            log.error(() -> String.format("Connection %d: client socket IO streams were not created due to the wrong encoding parameter.", cpId));
        } catch (IOException e) {
            log.error(() -> String.format("Connection %d: an IO error occurred when creating the client socket IO streams.", cpId));
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (Throwable t) { /* do nothing*/ }
//            try {
//                if (reader != null) reader.close();
//            } catch (Throwable t) { /* do nothing*/ }
            log.debug(() -> String.format("Connection %d has been closed (both IO streams closed).", cpId));
        }
    }

    protected abstract Response getResponse(Request request);

    protected void send(Response r) {
        if (r == null) {
            log.debug(() -> String.format("Connection %d: no need in a server response because the client request is null.", cpId));
            return;
        }

        val header = new StringBuilder(r.getHttpVersion()).append(' ').append(r.getStatus()).append("\r\n");
        for (val pair : r.getHeaders().entrySet())
            header.append(pair.getKey()).append(": ").append(pair.getValue()).append("\r\n");
        header.append("\r\n");

        try {
            writer.write(header.toString().getBytes(HTTP_CHARSET));
            writer.flush();
        } catch (IOException e) {
            log.error(() -> String.format("Connection %d: an IO error occurred when sending the response from the server to the client.", cpId));
        }
        log.trace(() -> String.format("Connection %d: the server response header has been sent.", cpId));

        if (r.getBody() != null) {
            try (val body = new BufferedInputStream(r.getBody())) {
                byte[] buffer = new byte[10 * 1024]; // 10 KB
                for (int bytesRead = -1; (bytesRead = body.read(buffer)) != -1; ) writer.write(buffer, 0, bytesRead);
            } catch (IOException e) {
                log.error(() -> String.format("Connection %d: an IO error occurred when sending the response from the server to the client.", cpId));
            }
        }

        log.debug(() -> String.format("Connection %d: the server response has been sent.", cpId));
    }

    // This method is made static for the convenience of tests
    static Request getRequest(BufferedReader reader, int id) {
        try {
            log.trace(() -> String.format("Connection %d: the client request parsing started.", id));

            String line = reader.readLine().trim();

            log.trace(() -> String.format("Connection %d: the first line of the client request has been read.", id));

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
            if (method != HttpMethod.GET && method != HttpMethod.HEAD) {
                log.debug(() -> String.format("Connection %d: currently only GET and HEAD methods are supported.", id));
                return null;
            }

            String path = terms[1];
            String httpVersion = terms[2];

            val headers = readHeaders(reader, id);
            log.trace(() -> String.format("Connection %d: all headers of the client request has been read.", id));
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

            val query = url.getQuery();
            Map<String, String> params = (query == null) ? Collections.emptyMap() : getParameters(url.getQuery(), id);

            val body = new StringBuilder();
            while (reader.ready() && (line = reader.readLine()) != null) body.append(line);

            log.debug(() -> String.format("Connection %d: the client request has been successfully parsed.", id));
            return Request.build(method, url.getPath(), httpVersion, params, url.getHost(), url.getPort(), headers, body.toString());

        } catch (IllegalArgumentException e) {
            log.error(() -> String.format("Connection %d: method in the client request doesn't exist in the HTTP specification.", id));
            return null;
        } catch (IOException e) {
            log.error(() -> String.format("Connection %d: an IO error occurred when reading the client request.", id));
            return null;
        }
    }

    private static Map<String, String> getParameters(String query, int id) {
        Map<String, String> params = new LinkedHashMap<>();
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length > 1) params.put(pair[0], pair[1]);
            else
                log.trace(() -> String.format("Connection %d: parameter '%s' has no value in the client request path query.", id, pair[0]));
        }
        return params;
    }

    private static Map<String, String> readHeaders(BufferedReader reader, int id) throws IOException {
        Map<String, String> headers = new LinkedHashMap<>();
        for (String s = null; reader.ready() && (s = reader.readLine()) != null && !s.trim().isEmpty(); ) {
            String[] header = s.split("\\s*:\\s*", 2);
            if (header.length > 1) {
                headers.put(header[0], header[1]);
                log.trace(() -> String.format("Connection %d: the header '%s' with value '%s' has been read.", id, header[0], header[1]));
            } else
                log.debug(() -> String.format("Connection %d: header '%s' has no value in the client request.", id, header[0]));
        }
        return headers;
    }
}
