package com.basepro.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理：统一把异常翻译成 {@link R}，前端只需读取 msg。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：401 / 403 同步到 HTTP 状态码，便于前端统一走刷新令牌、跳登录页的逻辑
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<R<Void>> handleBizException(BizException ex) {
        HttpStatus status = switch (ex.getCode()) {
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.OK;
        };
        return ResponseEntity.status(status).body(R.fail(ex.getCode(), ex.getMessage()));
    }

    /**
     * 参数校验失败：@Valid 修饰的请求体
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return R.fail(ErrorCode.BAD_REQUEST.getCode(), firstFieldMessage(ex.getBindingResult().getFieldError()));
    }

    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException ex) {
        return R.fail(ErrorCode.BAD_REQUEST.getCode(), firstFieldMessage(ex.getFieldError()));
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    public R<Void> handleConstraintViolation(Exception ex) {
        String msg = ex instanceof ConstraintViolationException cve && !cve.getConstraintViolations().isEmpty()
                ? cve.getConstraintViolations().iterator().next().getMessage()
                : ErrorCode.BAD_REQUEST.getMsg();
        return R.fail(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class})
    public R<Void> handleBadRequest(Exception ex) {
        log.warn("[请求参数错误] {}", ex.getMessage());
        return R.fail(ErrorCode.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return R.fail(ErrorCode.METHOD_NOT_ALLOWED.getCode(), ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public R<Void> handleNoResourceFound(NoResourceFoundException ex) {
        return R.fail(ErrorCode.NOT_FOUND.getCode(), ex.getMessage());
    }

    /**
     * 方法级权限校验失败（@PreAuthorize）
     */
    @ExceptionHandler(AccessDeniedException.class)
    public R<Void> handleAccessDenied(HttpServletRequest request, AccessDeniedException ex) {
        log.warn("[无权限] {} {}", request.getMethod(), request.getRequestURI());
        return R.fail(ErrorCode.FORBIDDEN);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public R<Void> handleDuplicateKey(DuplicateKeyException ex) {
        log.warn("[唯一索引冲突] {}", ex.getMessage());
        return R.fail(ErrorCode.BAD_REQUEST.getCode(), "数据已存在，请勿重复提交");
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(HttpServletRequest request, Exception ex) {
        log.error("[系统异常] {} {}", request.getMethod(), request.getRequestURI(), ex);
        return R.fail(ErrorCode.INTERNAL_ERROR);
    }

    private String firstFieldMessage(FieldError fieldError) {
        if (fieldError == null) {
            return ErrorCode.BAD_REQUEST.getMsg();
        }
        return fieldError.getDefaultMessage();
    }

}
