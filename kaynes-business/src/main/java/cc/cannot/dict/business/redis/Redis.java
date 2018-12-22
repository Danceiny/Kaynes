package cc.cannot.dict.business.redis;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface Redis {
    /**
     * 设置string值
     */
    void setStr(final String key, String value, long expire, TimeUnit unit);

    /**
     * 将对象序列化成json字符串后设置string值
     */
    void setAsJson(String key, Object value, long expire, TimeUnit unit);

    /**
     * 将json反序列化为指定java对象
     */
    <T> T getObj(String key, Class<T> cls);

    <T> T getObj(String key, Type cls);

    /**
     * 得到字符串值
     */
    String getStr(final String key);

    List<String> mget(final List<String> keys);

    void mset(final Map<String, String> map);

    Number getNumber(final String key);

    void setNumber(final String key, Number value);

    void incr(final String key, Number num);

    void remove(final String... keys);

    void remove(final String key);

    boolean exists(final String key);

    <T> T hgetObj(final String key, final String field, final Type type);

    <T> void hsetAsJson(final String key, final String field, final T value);

    void hmSet(final String key, final Map<String, String> map);

    <T> void hmSetAsJson(final String key, final Map<String, T> map);

    Object[] hmGetObj(final String key, final Collection fields, final Type type);

    List<String> hmGet(final String key, final Collection fields);

    String hget(final String key, final String field);

    <T> T hget(final String key, final String field, final Type type);

    void hdel(final String key, String... fields);
}
