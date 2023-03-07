package android.app;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class Application extends Context {
    private final PackageManager packageManager;
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
}
