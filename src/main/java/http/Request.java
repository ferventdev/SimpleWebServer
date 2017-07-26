package http;

/**
 * Created by Aleksandr Shevkunenko on 25.07.2017.
 */
public interface Request {

    enum HttpMethod { GET, HEAD }

    HttpMethod getMethod();

    static Request from(HttpMethod method) {
        return new Request() {
            @Override
            public HttpMethod getMethod() {
                return method;
            }
        };
    }
}
