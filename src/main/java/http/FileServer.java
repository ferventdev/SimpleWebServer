package http;

import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Aleksandr Shevkunenko on 28.07.2017.
 */
@Log4j2
public class FileServer extends ConnectionProcessor {

    private static final String baseDir = "src/main/resources/static/";
    static final String MESSAGE = "<html><head><meta charset=\"utf-8\"/></head><body><h1>404 - The requested file not found.</h1></body></html>";


    public FileServer(Socket clientSocket) {
        super(clientSocket);
    }

    @Override
    protected Response getResponse(Request req) {
        if (req == null) return null;
        log.info(() -> String.format("Connection %d: the client request (received by the server) is the following:%n%n%s", cpId, req.toString()));

        InputStream bodyStream = null;

        try {
            String status = "";
            Map<String, String> headers = new LinkedHashMap<>();
            Path filePath = Paths.get(baseDir, (req.getPath().equals("/")) ? "index.html" : req.getPath() );

            headers.put("Date", ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
            headers.put("Server", "Simple Web Server");

            if (!Files.isReadable(filePath) || !Files.isRegularFile(filePath)) {
                log.debug(() -> String.format("Connection %d: file '%s' either doesn't exist or it can't be read (JVM has insufficient privileges).", cpId, filePath.toString()));
                byte[] bodyBytes = MESSAGE.getBytes(HTTP_CHARSET);
                log.trace(() -> String.format("Connection %d: MESSAGE contains %d bytes.", cpId, bodyBytes.length));
                status = "404 Not Found";
                headers.put("Content-Length", Integer.toString(bodyBytes.length));
                bodyStream = new ByteArrayInputStream(bodyBytes);
            } else {
                status = "200 OK";
                headers.put("Content-Type", "text/html");
                headers.put("Content-Length", Long.toString(Files.size(filePath)) );
                headers.put("Content-Language", "en, ru");
                headers.put("Last-Modified", Files.getLastModifiedTime(filePath).toString());
                headers.put("Content-Encoding", "gzip");
                bodyStream = new FileInputStream(filePath.toFile());
            }

            return Response.build("HTTP/1.1", status, headers,
                    (req.getMethod() == Request.HttpMethod.HEAD) ? null : bodyStream);

        } catch (UnsupportedEncodingException e) {
            log.error(() -> String.format("Connection %d: the supplied encoding is not supported.", cpId));
        } catch (IOException e) {
            log.error(() -> String.format("Connection %d: an IO error occurred while constructing the server response.", cpId));
        } finally {
            log.debug(() -> String.format("Connection %d: the server response has been constructed.", cpId));
        }
        return null;
    }
}
