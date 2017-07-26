package http;

/**
 * Created by Aleksandr Shevkunenko on 26.07.2017.
 */
public interface Response {

    enum Status { OK }

    Status getStatus();

    static Response from(Status status) {
        return new Response() {
            @Override
            public Status getStatus() {
                return status;
            }
        };
    }
}
