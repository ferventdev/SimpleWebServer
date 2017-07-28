package http;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by Aleksandr Shevkunenko on 26.07.2017.
 */
public interface Response {

//    enum Status { OK }

    String getHttpVersion();
    String getStatus();
    Map<String, String> getHeaders();
    InputStream getBody();

    static Response from(String httpVersion, String status, Map<String, String> headers, InputStream body) {
        return new Response() {
            @Override
            public String getHttpVersion() {
                return httpVersion;
            }

            @Override
            public String getStatus() {
                return status;
            }

            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }

            @Override
            public InputStream getBody() {
                return body;
            }
        };
    }
}
