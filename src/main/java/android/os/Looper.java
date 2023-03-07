package android.os;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;

public class Looper {
    private Looper() {
    }
    private static final Looper mainLooper = new Looper();
    public static Looper getMainLooper() {
        return mainLooper;
    }

    public void post(Runnable r) {
        THREAD_POOL_EXECUTOR.execute(r);
    }
}
