package cc.cannot.dict.business.exception;

import cc.cannot.ms.springtime.modules.base.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ArgException extends BaseException {

    public ArgException(String message) {
        super(HttpStatus.BAD_REQUEST.value(), message);
    }

    @Override
    public Integer httpStatus() {
        return HttpStatus.BAD_REQUEST.value();
    }
}
