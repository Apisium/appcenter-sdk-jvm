package android.content;

import android.app.Application;
import android.content.pm.PackageManager;

public abstract class Context {
    public Application getApplicationContext() {
        return (Application) this;
    }
    abstract public PackageManager getPackageManager();

    abstract public String getPackageName();
}
