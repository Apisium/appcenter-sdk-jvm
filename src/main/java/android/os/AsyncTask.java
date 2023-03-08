package android.os;

import java.util.concurrent.*;

public abstract class AsyncTask <Params, Progress, Result> {
    private static int id = 0;
    public static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(0, 10,
            1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), r -> new Thread(r, "AsyncTask-" + r.hashCode() + "-" + id++));

    private FutureTask<Void> task;

    @SafeVarargs
    public final void executeOnExecutor(Executor executor, Params... params) {
        task = new FutureTask<>(() -> {
            onPreExecute();
            Result result = doInBackground(params);
            onPostExecute(result);
            return null;
        });
        executor.execute(task);
    }

    abstract protected Result doInBackground(Params... params);

    abstract protected void onPreExecute();

    abstract protected void onPostExecute(Object result);

    abstract protected void onCancelled(Object result);

    public final boolean cancel(boolean mayInterruptIfRunning) {
        boolean result = task.cancel(mayInterruptIfRunning);
        onCancelled(null);
        return result;
    }

    public final boolean isCancelled() {
        return task.isCancelled();
    }
}
