package android.os;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class Looper implements Executor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Looper.class);
    private int id = 0;
    private final Thread.UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER = (t, e) -> {
        if (!(e instanceof RejectedExecutionException)) LOGGER.error("AsyncTask thread (" + t.getName() + ") crashed.", e);
    };
    private final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(0, 10,
            1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), r -> {
        Thread th = new Thread(r, "AsyncTask-" + r.hashCode() + "-" + id++);
        th.setUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER);
        return th;
    });
    private Looper() {
    }
    private static final Looper mainLooper = new Looper();
    public static Looper getMainLooper() {
        return mainLooper;
    }

    public void post(Runnable r) {
        try {
            THREAD_POOL_EXECUTOR.execute(r);
        } catch (RejectedExecutionException ignored) { } catch (Throwable e) {
            LOGGER.error("AsyncTask (" + r + ") failed to execute.", e);
        }
    }

    public void close() {
        try {
            if (!THREAD_POOL_EXECUTOR.awaitTermination(20, TimeUnit.SECONDS)) {
                THREAD_POOL_EXECUTOR.shutdown();
            }
        } catch (Throwable e) {
            LOGGER.error("AsyncTask thread pool failed to shutdown.", e);
        }
    }

    @Override
    public void execute(@NotNull Runnable command) {
        post(command);
    }
}
