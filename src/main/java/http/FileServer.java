package http;

import java.net.Socket;

/**
 * Created by Aleksandr Shevkunenko on 28.07.2017.
 */
public class FileServer extends ConnectionProcessor {

    public FileServer(Socket clientSocket) {
        super(clientSocket);
    }

    @Override
    protected Response getResponse(Request request) {
        return null;
    }

    @Override
    protected void send(Response response) {

    }
}
