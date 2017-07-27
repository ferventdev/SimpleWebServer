package http;

import lombok.extern.log4j.Log4j2;

import java.net.Socket;

/**
 * Created by Aleksandr Shevkunenko on 26.07.2017.
 */
@Log4j2
public class GreetingServer extends ConnectionProcessor {

    static final String GREET =
            "<html><head><meta charset=\"utf-8\"/></head><body><h1>Greetings! Приветствия!</h1></body></html>";

    public GreetingServer(Socket clientSocket) {
        super(clientSocket);
    }

    @Override
    protected Response getResponse(Request request) {
        if (request == null) return null;
        log.info(() -> String.format("Connection %d: the client request (received by the server) is the following:%n%n%s", cpId, request.toString()));
        log.debug(() -> String.format("Connection %d: the server response has been constructed.", cpId));
        return Response.from(Response.Status.OK);
    }

    @Override
    protected void send(Response response) {
//        if (response == null) return;
//        super.send(response);
        writer.print(String.format(RESPONSE_HEADER, GREET.length(), GREET));
        writer.flush();
        log.debug(() -> String.format("Connection %d: the server response has been sent.", cpId));
    }
}
