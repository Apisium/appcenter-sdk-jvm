# Unofficial AppCenter JVM SDK [![Jitpack](https://www.jitpack.io/v/Apisium/appcenter-sdk-jvm.svg)](https://www.jitpack.io/#Apisium/appcenter-sdk-jvm)

Allows you to use the API of [AppCenter Android SDK](https://learn.microsoft.com/en-us/appcenter/) on the desktop jvm.

## Usage

### build.gradle

```groovy
repositories {
    maven { url 'https://www.jitpack.io' }
}

dependencies {
    implementation 'com.github.Apisium:appcenter-sdk-jvm:<version>'
}
```

### Main.java

```java
import android.app.Application;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.crashes.model.TestCrashException;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Application app = Application.getApplication(Main.class.getPackageName(), 1, "1.0.0");
        AppCenter.start(app, "YOUR APP SECRET", Crashes.class, Analytics.class);
        app.start();

        Analytics.trackEvent("test");
        Crashes.trackError(new TestCrashException());

        Thread.sleep(10000); // wait for the crash report to be sent
        app.close();
    }
}
```

## Author

Shirasawa

## License

[LICENSE](license.txt)