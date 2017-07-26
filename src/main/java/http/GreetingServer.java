package http;

import java.net.Socket;

/**
 * Created by Aleksandr Shevkunenko on 26.07.2017.
 */
public class GreetingServer extends ConnectionProcessor {

    static final String GREET =
            "<html><head><meta charset=\"utf-8\"/></head><body><h1>Greetings! Приветствия!</h1></body></html>";

    public GreetingServer(Socket clientSocket) {
        super(clientSocket);
    }

    @Override
    protected Response getResponse(Request request) {
        return Response.from(Response.Status.OK);
    }

    @Override
    protected void send(Response response) {
//        super.send(response);
        writer.print(String.format(RESPONSE_HEADER, GREET.length(), GREET));
        writer.flush();
    }
}
