package cc.cannot.common.utils;

import java.util.Collection;

/**
 * 由于各个库（比如spring, apache.common.lang3等）中的StringUtils中实现的同名函数行为表现不一致，
 * 因此希望在这里统一标准
 */
public class StrUtils {
    private StrUtils() {

    }

    public static String[] split(String str, String separatorChars) {
        return org.apache.commons.lang3.StringUtils.split(str, separatorChars);
    }

    public static <T> String join(T[] array, String sep) {
        return join(array, 0, -1, sep, null);
    }

    public static <T> String join(T[] array, String sep, int from, int to) {
        return join(array, from, to, sep, null);
    }

    public static String join(Collection list, String sep) {
        return join(list, sep, null);
    }

    public static String join(Collection list, String sep, String prefix) {
        Object[] array = list == null ? null : list.toArray();
        return join(array, 0, -1, sep, prefix);
    }

    /**
     * 以指定的分隔符来进行字符串元素连接，支持数组slice
     * 例如有字符串数组array和连接符为逗号(,)
     * <code>
     * String[] array = new String[] { "hello", "world", "qiniu", "cloud","storage" };
     * </code>
     * 那么得到的结果是:
     * <code>
     * hello,world,qiniu,cloud,storage
     * </code>
     */
    public static String join(Object[] array, int from, int to, String sep, String prefix) {
        if (array == null) {
            return "";
        }
        int arraySize = array.length;
        if (arraySize == 0) {
            return "";
        }
        if (from < 0 || from > arraySize - 1) {
            from = 0;
        }
        if (to < 0 || to > arraySize) {
            to = arraySize;
        }
        if (sep == null) {
            sep = "";
        }
        if (prefix == null) {
            prefix = "";
        }
        StringBuilder buf = new StringBuilder(prefix);
        buf.append(array[from] == null ? "" : array[from]);
        for (int i = from + 1; i < to; i++) {
            buf.append(sep).append(array[i] == null ? "" : array[i]);
        }
        return buf.toString();
    }

    /**
     * 以json元素的方式连接字符串中元素
     * <p>
     * 例如有字符串数组array
     * <code>
     * String[] array = new String[] { "hello", "world", "qiniu", "cloud","storage" };
     * </code>
     * 那么得到的结果是:
     * <code>
     * "hello","world","qiniu","cloud","storage"
     * </code>
     * </p>
     *
     * @param array 需要连接的字符串数组
     * @return 以json元素方式连接好的新字符串
     */
    public static String jsonJoin(String[] array) {
        int arraySize = array.length;
        int bufSize = arraySize * (array[0].length() + 3);
        StringBuilder buf = new StringBuilder(bufSize);
        for (int i = 0; i < arraySize; i++) {
            if (i > 0) {
                buf.append(',');
            }
            buf.append('"');
            buf.append(array[i]);
            buf.append('"');
        }
        return buf.toString();
    }
}
