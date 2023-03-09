package android.content.pm;

public class PackageManager {
    public final PackageInfo packageInfo;
    public PackageManager(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public PackageInfo getPackageInfo(String packageName, int flags) {
        return packageInfo;
    }
}
