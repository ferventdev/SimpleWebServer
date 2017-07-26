package http;

import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aleksandr Shevkunenko on 25.07.2017.
 */
@Log4j2
public class Server {

    private static final int DEFAULT_PORT = 8080;
    private static final int SOCKET_ACCEPT_TIMEOUT = 1 * 2 * 1000; // 5 minutes

    public static void main(String... args) {
        int port = DEFAULT_PORT;
        try {
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
                if (port <= 0 || port > 65535) {
                    final int finalPort = port; // port can't be used in lambda directly, because it's not effectively final
                    log.warn(() -> String.format("The supplied port number (%d) is outside the valid range. The default value (%d) will be applied.", finalPort, DEFAULT_PORT));
                    port = DEFAULT_PORT;
                }
            }
        } catch (NumberFormatException e) {
            log.warn(() -> String.format("The supplied port number (%s) is not a parsable integer. The default value (%d) will be applied.", args[0], DEFAULT_PORT));
        }

        int maxThreads = Runtime.getRuntime().availableProcessors() * 1;
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        log.debug(() -> String.format("The executor pool for maximum of %d threads has been created.", maxThreads));

        final int finalPort = port; // port can't be used in lambda directly, because it's not effectively final
        boolean interrupted = Thread.currentThread().isInterrupted();

        try (val ss = new ServerSocket(port)) {
            log.info(() -> String.format("The server successfully started listening to the port number %d.", finalPort));

            ss.setSoTimeout(SOCKET_ACCEPT_TIMEOUT); // timeout so that the blocking accept call doesn't wait forever
            boolean timeout = false;
            while(!timeout && !interrupted)
                try {

                    ConnectionProcessor cp = new GreetingServer(ss.accept());
                    executor.execute(cp);

                } catch (SocketTimeoutException e) {
                    timeout = false; //true;
                    log.info(() -> "The client's connection timeout is over.");
                } catch (RejectedExecutionException e) {
                    log.warn(() -> "The task cannot be accepted for execution.");
                    TimeUnit.SECONDS.sleep(2);
                } catch (IOException e) {
                    log.error(() -> "An IO error occurred while waiting for a connection.");
                } finally {
                    interrupted = Thread.currentThread().isInterrupted();
                }

        } catch (InterruptedException e) {
            interrupted = true;
        } catch (IOException e) {
            log.error(() -> "An IO error occurred when opening the server socket.");
//            log.error(e.getMessage());
        } finally {
            if (interrupted) log.debug(() -> "The server thread was interrupted.");
            log.info(() -> String.format("The server has stopped listening to the port number %d.", finalPort));
            executor.shutdown();
            log.debug(() -> "All submitted tasks in the executor pool were executed. The pool was shut down.");
        }
    }
}