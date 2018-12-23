package cc.cannot.common.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ArgException extends BaseException {
    public ArgException(String msg) {
        super(msg);
    }
}
