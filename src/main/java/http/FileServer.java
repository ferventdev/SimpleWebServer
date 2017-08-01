package http;

import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class implements a simple file server.
 * It allows to download static files (html, text, pics, scripts, etc.).
 */
@Log4j2
public class FileServer extends ConnectionProcessor {

    private static final String baseDir = "src/main/resources/static/";
    static final String MESSAGE = "<html><head><meta charset=\"utf-8\"/></head><body><h1>404 - The requested file not found.</h1></body></html>";

    static final Map<String, String> contents = new HashMap<>();

    static {

        contents.put("txt", "text/plain");
        contents.put("rtf", "application/rtf");
        contents.put("css", "text/css");
        contents.put("htm", "text/html");
        contents.put("html", "text/html");
        contents.put("xhtml", "application/xhtml+xml");
        contents.put("xht", "application/xhtml+xml");
        contents.put("xml", "application/xml");
        contents.put("md", "text/markdown");
        contents.put("markdown", "text/markdown");
        contents.put("php", "text/php");
        contents.put("php3", "text/php");
        contents.put("php4", "text/php");
        contents.put("php5", "text/php");
        contents.put("phps", "text/php");
        contents.put("phtml", "text/php");

        contents.put("js", "application/javascript");
        contents.put("json", "application/json");
        contents.put("pdf", "application/pdf");
        contents.put("zip", "application/zip");
        contents.put("gz", "application/gzip");

        contents.put("gif", "image/gif");
        contents.put("jpg", "image/jpeg");
        contents.put("jpe", "image/jpeg");
        contents.put("png", "image/png");
        contents.put("tif", "image/tiff");
        contents.put("tiff", "image/tiff");
    }


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

            headers.put("Server", "Simple Web Server");
            headers.put("Date", ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));

            if (!Files.isReadable(filePath) || !Files.isRegularFile(filePath)) {
                log.debug(() -> String.format("Connection %d: file '%s' either doesn't exist or it can't be read (JVM has insufficient privileges).", cpId, filePath.toString()));
                byte[] bodyBytes = MESSAGE.getBytes(HTTP_CHARSET);
                log.trace(() -> String.format("Connection %d: MESSAGE contains %d bytes.", cpId, bodyBytes.length));
                status = "404 Not Found";
                headers.put("Content-Length", Integer.toString(bodyBytes.length));
                bodyStream = new ByteArrayInputStream(bodyBytes);
            } else {
                status = "200 OK";
                String[] terms = filePath.getFileName().toString().split("\\.");
                String extension = null;
                if (terms.length > 1 && contents.containsKey( (extension = terms[terms.length - 1]) ) ) {
                    headers.put("Content-Type", contents.get(extension));
                    log.debug(() -> String.format("Connection %d: 'Content-Type' is '%s'.", cpId, headers.get("Content-Type")) );
                }
                headers.put("Content-Length", Long.toString(Files.size(filePath)) );
                headers.put("Content-Language", "en, ru");
                headers.put("Last-Modified", Files.getLastModifiedTime(filePath).toString());
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
