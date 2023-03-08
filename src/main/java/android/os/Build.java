package android.os;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

public class Build {
    public static final String OS_NAME;
    public static final String MODEL;
    public static final String MANUFACTURER = System.getProperty("java.vendor");
    public static final String ID = System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.version") + ")";
    public static final Versions VERSION;

    static {
        SystemInfo systemInfo = new SystemInfo();
        OperatingSystem os = systemInfo.getOperatingSystem();
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        OS_NAME = os.getManufacturer();
        MODEL = hardware.getProcessor().getProcessorIdentifier().getName();

        int sdkVersion = 0;
        try {
            sdkVersion = Integer.parseInt(System.getProperty("java.specification.version").replace("1.", ""));
        } catch (Throwable e) {
            // ignore
        }
        VERSION = new Versions(sdkVersion, os.getFamily() + " " + os.getVersionInfo());
    }

    public static class Versions {
        public final int SDK_INT;
        public final String RELEASE;

        private Versions(int sdkVersion, String release) {
            SDK_INT = sdkVersion;
            RELEASE = release;
        }
    }
}
