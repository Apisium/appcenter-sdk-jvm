package com.microsoft.appcenter;

import android.app.Application;
import android.content.pm.PackageInfo;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.crashes.model.TestCrashException;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        AppCenter.start(
                Application.getApplication("com.eimsound.daw", 1, "1.0.0"),
                "43136bd6-e32f-407c-b90d-b40a9a8cd230",
                Crashes.class
        );
        Crashes.trackError(new TestCrashException());
        Thread.sleep(100000);
    }
}