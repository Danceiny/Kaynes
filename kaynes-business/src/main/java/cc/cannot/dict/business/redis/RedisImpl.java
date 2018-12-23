package cc.cannot.dict.business.redis;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import cc.cannot.common.utils.PrimeTypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisImpl implements Redis {
    private final RedisTemplate<String, String> redisStrTemplate;

    private final RedisTemplate<String, Number> redisNumberTemplate;

    public RedisImpl(RedisTemplate<String, Number> redisNumberTemplate,
                     @Qualifier("redisStringTemplate") RedisTemplate<String, String> redisStrTemplate) {
        this.redisNumberTemplate = redisNumberTemplate;
        this.redisStrTemplate = redisStrTemplate;
    }

    @Override
    public void setStr(final String key, String value, long expire, TimeUnit unit) {
        redisStrTemplate.opsForValue().set(key, value);
        redisStrTemplate.expire(key, expire, unit);
    }

    @Override
    public void setAsJson(final String key, Object value, long expire, TimeUnit unit) {
        redisStrTemplate.opsForValue().set(key, JSONObject.toJSONString(value));
        redisStrTemplate.expire(key, expire, unit);
    }

    @Override
    public <T> T getObj(final String key, Class<T> clazz) {
        String json = redisStrTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return JSONObject.parseObject(json, clazz);
        } catch (JSONException e) {
            log.error(String.format("json解析失败:%s %n%s <=%n %s", e.getMessage(), clazz, json));
            throw new ClassCastException(clazz.getCanonicalName());
        }
    }


    @Override
    public <T> T getObj(final String key, final Type cls) {
        String json = redisStrTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return JSONObject.parseObject(json, cls);
        } catch (JSONException e) {
            log.error(String.format("json解析失败:%s %n%s <=%n %s", e.getMessage(), cls, json));
            throw new ClassCastException(cls.getTypeName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void incr(final String key, Number num) {
        redisNumberTemplate.opsForValue().increment(key, num.doubleValue());
    }

    /**
     * 批量删除对应的value
     */
    @Override
    public void remove(final String... keys) {
        for (String key : keys) {
            remove(key);
        }
    }

    /**
     * 删除对应的value
     */
    @Override
    @SuppressWarnings("unchecked")
    public void remove(final String key) {
        if (exists(key)) {
            redisStrTemplate.delete(key);
        }
    }

    /**
     * 判断缓存中是否有对应的value
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean exists(final String key) {
        return PrimeTypeUtils.boolValue(redisStrTemplate.hasKey(key));
    }

    @Override
    public <T> T hgetObj(final String key, final String field, final Type type) {
        String s = (String) redisStrTemplate.opsForHash().get(key, field);
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        return JSONObject.parseObject(s, type);
    }

    @Override
    public <T> void hsetAsJson(final String key, final String field, final T value) {
        redisStrTemplate.opsForHash().put(key, field, JSONObject.toJSONString(value));
    }

    @Override
    public void hmSet(final String key, final Map<String, String> map) {
        redisStrTemplate.opsForHash().putAll(key, map);
    }

    @Override
    public <T> void hmSetAsJson(final String key, final Map<String, T> map) {
        Map<String, String> stringMap = new HashMap<>();
        map.forEach((k, v) -> stringMap.put(k, JSONObject.toJSONString(v)));
        this.hmSet(key, stringMap);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object[] hmGetObj(final String key, final Collection fields, final Type type) {
        List list = redisStrTemplate.opsForHash().multiGet(key, fields);
        int l = list.size();
        Object[] arr = new Object[list.size()];
        for (int i = 0; i < l; i++) {
            arr[i] = JSONObject.parseObject((String) list.get(i), type);
        }
        return arr;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> hmGet(final String key, final Collection fields) {
        return redisStrTemplate.opsForHash().multiGet(key, fields);
    }

    @Override
    public String hget(final String key, final String field) {
        return (String) redisStrTemplate.opsForHash().get(key, field);
    }

    @Override
    public <T> T hget(final String key, final String field, final Type type) {
        return JSONObject.parseObject(this.hget(key, field), type);
    }

    @Override
    public void hdel(final String key, final String... fields) {
        if (fields.length == 0) {
            return;
        }
        redisStrTemplate.opsForHash().delete(key, (Object[]) fields);
    }

    @Override
    public String getStr(final String key) {
        return redisStrTemplate.opsForValue().get(key);
    }

    @Override
    public List<String> mget(final List<String> keys) {
        return redisStrTemplate.opsForValue().multiGet(keys);
    }

    @Override
    public void mset(final Map<String, String> map) {
        redisStrTemplate.opsForValue().multiSet(map);
    }

    @Override
    public Number getNumber(final String key) {
        return redisNumberTemplate.opsForValue().get(key);
    }

    @Override
    public void setNumber(final String key, Number value) {
        redisNumberTemplate.opsForValue().set(key, value);
    }

}
