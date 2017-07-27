package http;

import lombok.val;

import java.util.Map;

/**
 * Created by Aleksandr Shevkunenko on 25.07.2017.
 */
public interface Request {

    // all HTTP methods are enumerated, but actually only GET method is supported
    static enum HttpMethod { GET, HEAD, POST, PUT, PATCH, DELETE, TRACE, CONNECT, OPTIONS }

    HttpMethod getMethod();

    String getPath();

    String getProtocolVersion();

    Map<String, String> getParameters();

    String getHost();

    int getPort();

    Map<String, String> getHeaders();

    String getBody();

    default String intoString() {
        val req = new StringBuilder();
        req.append(getMethod()).append(" ").append(getPath());
        for (val pair : getParameters().entrySet()) req.append(pair.getKey()).append("=").append(pair.getValue());
        req.append(" ").append(getProtocolVersion()).append("\r\n");
        for (val pair : getHeaders().entrySet()) req.append(pair.getKey()).append(": ").append(pair.getValue()).append("\r\n");
        if (getBody() != null && !getBody().isEmpty()) req.append("\r\n").append(getBody());
        return req.toString();
    }

    static Request build(HttpMethod method, String path, String version,
                         Map<String, String> parameters, String host, int port,
                         Map<String, String> headers, String body) {

        return new Request() {
            @Override
            public HttpMethod getMethod() {
                return method;
            }

            @Override
            public String getPath() {
                return path;
            }

            @Override
            public String getProtocolVersion() {
                return version;
            }

            @Override
            public Map<String, String> getParameters() {
                return parameters;
            }

            @Override
            public String getHost() {
                return host;
            }

            @Override
            public int getPort() {
                return port;
            }

            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }

            @Override
            public String getBody() {
                return body;
            }
        };
    }
}
