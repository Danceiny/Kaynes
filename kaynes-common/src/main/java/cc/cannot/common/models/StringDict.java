package cc.cannot.common.models;


import com.alibaba.fastjson.JSONObject;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import static org.apache.commons.lang3.CharEncoding.UTF_8;


/**
 * 源自七牛的StringMap和fastjson中的JSONObject
 *
 * @author huangzhen
 */
public class StringDict extends JSONObject {
    public StringDict() {
        super();
    }

    public StringDict putNotEmpty(String key, String value) {
        if (!StringUtils.isEmpty(value)) {
            this.put(key, value);
        }
        return this;
    }

    public StringDict putNotEmpty(String key, Collection value) {
        if (!CollectionUtils.isEmpty(value)) {
            this.put(key, value);
        }
        return this;
    }

    public StringDict putNotNull(String key, Object value) {
        if (value != null) {
            this.put(key, value);
        }
        return this;
    }

    public StringDict putWhen(String key, Object val, boolean when) {
        if (when) {
            this.put(key, val);
        }
        return this;
    }

    public StringDict put(Map<String, Object> map) {
        this.putAll(map);
        return this;
    }

    public void removeAll(String... keys) {
        for (String k : keys) {
            this.remove(k);
        }
    }

    @Override
    public StringDict put(String key, Object value) {
        try {
            super.put(key, value);
        } catch (Exception e) {
            /*
             *      * @throws UnsupportedOperationException if the <tt>put</tt> operation
             *      *         is not supported by this map
             *      * @throws ClassCastException if the class of the specified key or value
             *      *         prevents it from being stored in this map
             *      * @throws NullPointerException if the specified key or value is null
             *      *         and this map does not permit null keys or values
             *      * @throws IllegalArgumentException if some property of the specified key
             *      *         or value prevents it from being stored in this map
             */
        }
        return this;
    }

    public StringDict(StringDict source) {
        this();
        if (source != null) {
            source.forEach(this::put);
        }
    }

    public boolean exist(String key) {
        return containsKey(key);
    }

    public String formString() {
        final StringBuilder b = new StringBuilder();
        final boolean[] notStart = {false};
        forEach((key, value) -> {
            if (value == null) {
                return;
            }
            if (notStart[0]) {
                b.append("&");
            }
            try {
                b.append(URLEncoder.encode(key, UTF_8)).append('=')
                 .append(URLEncoder.encode(value.toString(), UTF_8));
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
            notStart[0] = true;
        });
        return b.toString();
    }

    public String formString(boolean sorted) {
        if (sorted) {
            // 升序
            List<String> keys = new ArrayList<>(this.getInnerMap().keySet());
            Collections.sort(keys);
            StringBuilder b = new StringBuilder();
            boolean notStart = false;
            for (String key : keys) {
                if (notStart) {
                    b.append("&");
                }
                Object value = get(key);
                if (value == null) {
                    continue;
                }
                try {
                    b.append(URLEncoder.encode(key, UTF_8)).append('=')
                     .append(URLEncoder.encode(value.toString(), UTF_8));
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError(e);
                }

                notStart = true;
            }
            return b.toString();
        } else {
            return formString();
        }
    }

    @SuppressWarnings("unchecked")
    public SortedMap<String, String> toTreeMap() {
        return new TreeMap(this.getInnerMap());
    }
}
