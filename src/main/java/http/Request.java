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

            @Override
            public String toString() {
                val req = new StringBuilder();
                req.append(method).append(" ").append(path);
                for (val pair : parameters.entrySet()) req.append(pair.getKey()).append("=").append(pair.getValue());
                req.append(" ").append(version).append("\r\n");
                for (val pair : headers.entrySet()) req.append(pair.getKey()).append(": ").append(pair.getValue()).append("\r\n");
                if (body != null && !body.isEmpty()) req.append("\r\n").append(body);
                return req.toString();
            }
        };
    }
}
