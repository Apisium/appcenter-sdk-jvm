package android.os;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class SystemClock {
    private final static RuntimeMXBean RUNTIME_MX_BEAN = ManagementFactory.getRuntimeMXBean();
    public static long elapsedRealtime() {
        return RUNTIME_MX_BEAN.getUptime();
    }
}
