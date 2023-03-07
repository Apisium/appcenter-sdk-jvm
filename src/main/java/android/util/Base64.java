package android.util;

public class Base64 {
    public static final int DEFAULT = 0, NO_WRAP = 2;
    public static byte[] decode(String input, int flags) {
        return java.util.Base64.getDecoder().decode(input);
    }
    public static String encodeToString(byte[] input, int flags) {
        return java.util.Base64.getEncoder().encodeToString(input);
    }
}
