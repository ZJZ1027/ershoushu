package com.basepro.common;

import lombok.Getter;

/**
 * 业务异常。被 {@link GlobalExceptionHandler} 捕获后原样返回给前端提示。
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String msg) {
        this(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    public BizException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public BizException(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMsg());
    }

    /**
     * 业务异常无需堆栈，避免高频抛出时的性能损耗
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
