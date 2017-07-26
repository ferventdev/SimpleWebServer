package http;

import java.net.Socket;

/**
 * Created by Aleksandr Shevkunenko on 26.07.2017.
 */
public class GreetingServer extends ConnectionProcessor {
    public GreetingServer(Socket clientSocket) {
        super(clientSocket);
    }

    @Override
    protected Response getResponse(Request request) {
        return Response.from(Response.Status.OK);
    }

    @Override
    protected void send(Response response) {
        writer.print("Greetings from the server!\nПриветствия от сервера!");
    }
}
