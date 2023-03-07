package android.os;

public class Build {
    public static final String MODEL = System.getProperty("os.arch");
    public static final String MANUFACTURER = System.getProperty("java.vendor");
    public static final String ID = System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.version") + ")";
    public static final Versions VERSION = new Versions();

    public static class Versions {
        public static final int SDK_INT;
        public static final String RELEASE = System.getProperty("os.version");

        static {
            int version = 0;
            try {
                version = Integer.parseInt(System.getProperty("java.specification.version").replace("1.", ""));
            } catch (Throwable e) {
                // ignore
            }
            SDK_INT = version;
        }
    }
}
