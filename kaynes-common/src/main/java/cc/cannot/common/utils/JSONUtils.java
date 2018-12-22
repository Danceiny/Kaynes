package cc.cannot.dict.common.utils;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class JSONUtils {
    private JSONUtils() {

    }

    /**
     * fastjson默认不会序列化 transient 关键字修饰的字段，这里需要序列化
     */
    public static String toJSONStringIgnoreTransient(Object object) {
        try (SerializeWriter out = new SerializeWriter()) {
            JSONSerializer serializer = new JSONSerializer(out);
            serializer.config(SerializerFeature.SkipTransientField, false);
            serializer.write(object);
            return out.toString();
        }
    }
}
