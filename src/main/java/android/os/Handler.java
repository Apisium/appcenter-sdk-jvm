package android.os;

public class Handler {
    private final Looper mLooper;

    public Handler(Looper looper) {
        mLooper = looper;
    }

    public Looper getLooper() {
        return mLooper;
    }

    public void post(Runnable r) {
        mLooper.post(r);
    }
}
