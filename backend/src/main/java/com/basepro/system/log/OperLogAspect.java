package com.basepro.system.log;

import com.basepro.common.BizException;
import com.basepro.common.ErrorCode;
import com.basepro.common.R;
import com.basepro.common.WebUtils;
import com.basepro.security.LoginUser;
import com.basepro.security.SecurityUtils;
import com.basepro.system.entity.SysOperLog;
import com.basepro.system.mapper.SysOperLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志切面：记录写操作的入参、耗时与结果。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private static final int MAX_PARAMS_LENGTH = 2000;

    private final SysOperLogMapper operLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperLog operLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            int code = result instanceof R<?> r ? r.code() : R.SUCCESS;
            String msg = result instanceof R<?> r ? r.msg() : null;
            save(joinPoint, operLog, startTime, code, msg);
            return result;
        } catch (Throwable ex) {
            int code = ex instanceof BizException bizException
                    ? bizException.getCode() : ErrorCode.INTERNAL_ERROR.getCode();
            save(joinPoint, operLog, startTime, code, ex.getMessage());
            throw ex;
        }
    }

    private void save(ProceedingJoinPoint joinPoint, OperLog operLog, long startTime, int code, String msg) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            SysOperLog entity = new SysOperLog();
            entity.setModule(operLog.module());
            entity.setName(operLog.name());
            LoginUser loginUser = SecurityUtils.getLoginUserOrNull();
            if (loginUser != null) {
                entity.setUserId(loginUser.userId());
                entity.setUsername(loginUser.username());
            }
            HttpServletRequest request = WebUtils.getRequest();
            if (request != null) {
                entity.setRequestMethod(request.getMethod());
                entity.setRequestUrl(request.getRequestURI());
            }
            entity.setJavaMethod(signature.getDeclaringType().getSimpleName() + "." + signature.getName());
            entity.setUserIp(WebUtils.getClientIp());
            entity.setUserAgent(WebUtils.getUserAgent());
            entity.setDuration((int) (System.currentTimeMillis() - startTime));
            entity.setResultCode(code);
            entity.setResultMsg(truncate(msg, 512));
            if (operLog.saveParams()) {
                entity.setRequestParams(truncate(toJson(joinPoint.getArgs()), MAX_PARAMS_LENGTH));
            }
            entity.setCreateTime(LocalDateTime.now());
            operLogMapper.insert(entity);
        } catch (Exception ex) {
            // 记录日志失败不能影响业务
            log.warn("[操作日志记录失败] {}", ex.getMessage());
        }
    }

    private String toJson(Object[] args) {
        Object[] filtered = Arrays.stream(args)
                .filter(arg -> !(arg instanceof HttpServletRequest || arg instanceof HttpServletResponse
                        || arg instanceof MultipartFile))
                .toArray();
        return filtered.length == 0 ? null : objectMapper.writeValueAsString(filtered);
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

}
