package com.basepro.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 全局错误码。业务校验失败直接 throw new BizException("提示语") 即可，无需在此登记。
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    BAD_REQUEST(400, "请求参数不正确"),
    UNAUTHORIZED(401, "账号未登录"),
    FORBIDDEN(403, "没有该操作权限"),
    NOT_FOUND(404, "请求的数据不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不正确"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),
    INTERNAL_ERROR(500, "系统异常，请联系管理员");

    private final int code;
    private final String msg;

}
