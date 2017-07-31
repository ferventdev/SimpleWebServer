package http;

import lombok.extern.log4j.Log4j2;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Aleksandr Shevkunenko on 26.07.2017.
 */
@Log4j2
public class GreetingServer extends ConnectionProcessor {

    static final String GREET = "<html><head><meta charset=\"utf-8\"/></head><body><h1>Greetings! Приветствия!</h1></body></html>";

    public GreetingServer(Socket clientSocket) {
        super(clientSocket);
    }

    @Override
    protected Response getResponse(Request req) {
        if (req == null) return null;
        log.info(() -> String.format("Connection %d: the client request (received by the server) is the following:%n%n%s", cpId, req.toString()));

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "text/html");
        headers.put("Content-Length", Integer.toString(GREET.length()));
        headers.put("Connection", "close");

        log.debug(() -> String.format("Connection %d: the server response has been constructed.", cpId));

        try {

            byte[] greetBytes = GREET.getBytes(HTTP_CHARSET);
            log.trace(() -> String.format("Connection %d: GREET contains %d bytes.", cpId, greetBytes.length));

            return Response.build("HTTP/1.1", "200 OK", headers,
                    (req.getMethod() == Request.HttpMethod.HEAD) ? null : new ByteArrayInputStream(greetBytes));

        } catch (UnsupportedEncodingException e) {
            log.error(() -> String.format("Connection %d: the supplied encoding is not supported.", cpId));
        }
        return null;
    }
}
