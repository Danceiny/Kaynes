package cc.cannot.dict.api.resp;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huangzhen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonResp<T> {

    private static final Integer SUCCESS = 0;

    private static final Integer FAILED = 1;

    private static final Integer LIMITED = 2;

    private static final Integer EXCEPTION = 3;

    private Integer code;

    private String msg;

    private T data;

    public static <T> JsonResp error(String message, T data) {
        return buildJsonResult(FAILED, message, data);
    }

    public static JsonResp error(String message) {
        return buildJsonResult(FAILED, message, null);
    }

    public static JsonResp success() {
        return buildJsonResult(SUCCESS, null, null);
    }

    public static <T> JsonResp success(T data) {
        return buildJsonResult(SUCCESS, null, data);
    }

    private static <T> JsonResp buildJsonResult(Integer code, String message, T data) {
        return new JsonResp<>(code, message, data);
    }


}
