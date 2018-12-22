package cc.cannot.dict.exception;

import cc.cannot.dict.api.resp.JsonResp;
import cc.cannot.ms.springtime.modules.base.exception.BaseException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * 处理controller中抛出的异常
 */
@ControllerAdvice
@Slf4j
public class DictExceptionHandler {
    @ExceptionHandler({
            IllegalArgumentException.class,
            MySQLIntegrityConstraintViolationException.class,
            ServletRequestBindingException.class,
            HttpMediaTypeNotSupportedException.class,
            NumberFormatException.class,
            JsonParseException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Object argExceptionHandler(HttpServletRequest req, Exception e) {
        log.error(e.getMessage());
        return JsonResp.error(e.getMessage());
    }

    @ExceptionHandler({BaseException.class})
    @ResponseBody
    public Object serviceExceptionHandler(HttpServletRequest req, Exception e) {
        log.error(e.getMessage());
        return JsonResp.error(e.getMessage());
    }

    @ExceptionHandler({UnsupportedOperationException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public Object unsupportExceptionHandler(HttpServletRequest req, Exception e) {
        log.warn(e.getMessage());
        return JsonResp.error(e.getMessage());
    }
}
