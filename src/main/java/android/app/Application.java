package android.app;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

public class Application extends Context {
    private boolean started = false;
    private final PackageManager packageManager;
    private final Activity activity = new Activity();
    private final Bundle bundle = new Bundle();
    private final List<ActivityLifecycleCallbacks> activityLifecycleCallbacks = new ArrayList<>();
    private Application(PackageInfo packageInfo) {
        packageManager = new PackageManager(packageInfo);
    }

    @Override
    public PackageManager getPackageManager() {
        return packageManager;
    }

    @Override
    public String getPackageName() {
        return packageManager.packageInfo.packageName;
    }

    public static Application getApplication(String packageName, int versionCode, String versionName) {
        return new Application(new PackageInfo(packageName, versionCode, versionName));
    }

    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        activityLifecycleCallbacks.add(callback);
    }

    public void start() {
        if (started) return;
        started = true;
        for (ActivityLifecycleCallbacks callback : activityLifecycleCallbacks) {
            callback.onActivityCreated(activity, bundle);
            callback.onActivityStarted(activity);
            callback.onActivityResumed(activity);
        }
    }

    public void close() {
        if (!started) return;
        for (ActivityLifecycleCallbacks callback : activityLifecycleCallbacks) {
            callback.onActivityPaused(activity);
            callback.onActivityStopped(activity);
            callback.onActivityDestroyed(activity);
        }
        Looper.getMainLooper().close();
    }

    public interface ActivityLifecycleCallbacks {
        void onActivityCreated(Activity activity, Bundle savedInstanceState);
        void onActivityStarted(Activity activity);
        void onActivityResumed(Activity activity);
        void onActivityPaused(Activity activity);
        void onActivityStopped(Activity activity);
        void onActivitySaveInstanceState(Activity activity, Bundle outState);
        void onActivityDestroyed(Activity activity);
    }
}
