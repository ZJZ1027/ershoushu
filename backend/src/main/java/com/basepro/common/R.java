package com.basepro.common;

/**
 * 统一响应结果。前端约定：code = 200 视为成功，其余弹出 msg 提示。
 */
public record R<T>(int code, String msg, T data) {

    public static final int SUCCESS = 200;

    public static <T> R<T> ok(T data) {
        return new R<>(SUCCESS, "", data);
    }

    public static R<Void> ok() {
        return new R<>(SUCCESS, "", null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }

    public static <T> R<T> fail(ErrorCode errorCode) {
        return new R<>(errorCode.getCode(), errorCode.getMsg(), null);
    }

}
