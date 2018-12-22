package cc.cannot.dict.common.utils;

/**
 * 基本数据类型装箱后防止空指针的辅助方法
 * null-safe for primitive values
 */
public class PrimeTypeUtils {
    private PrimeTypeUtils() {

    }

    public static int intValue(Integer i) {
        if (i == null) {
            return 0;
        }
        return i;
    }

    public static int shortValue(Short s) {
        if (s == null) {
            return 0;
        }
        return s;
    }

    public static long longValue(Long l) {
        if (l == null) {
            return 0L;
        }
        return l;
    }

    public static boolean boolValue(Boolean b) {
        if (b == null) {
            return false;
        }
        return b;
    }

    /**
     * 前者是否大于后者
     */
    public static boolean gt(Integer a, Integer b) {
        return intValue(a) > intValue(b);
    }

    public static boolean gt(Short a, Short b) {
        return shortValue(a) > shortValue(b);
    }

    /**
     * 前者是否小于后者
     */
    public static boolean lt(Integer a, Integer b) {
        return intValue(a) < intValue(b);
    }

    public static boolean lt(Short a, Short b) {
        return shortValue(a) < shortValue(b);
    }


}
