package http;

import java.io.InputStream;
import java.util.Map;

/**
 * This interface is used to build a server response object and then to refer the data in it if necessary
 * (for example, when sending out this response).
 */
public interface Response {

    String getHttpVersion();
    String getStatus();
    Map<String, String> getHeaders();
    InputStream getBody();

    static Response build(String httpVersion, String status, Map<String, String> headers, InputStream body) {
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
