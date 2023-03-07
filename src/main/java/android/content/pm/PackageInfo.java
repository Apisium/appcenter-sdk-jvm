package android.content.pm;

public class PackageInfo {
    public String packageName;
    public int versionCode;
    public String versionName;

    public PackageInfo(String packageName, int versionCode, String versionName) {
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.versionName = versionName;
    }
}
