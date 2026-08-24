package com.basepro.system.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要记录操作日志的接口，由 {@link OperLogAspect} 落库到 sys_oper_log。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLog {

    /**
     * 模块名，如“用户”
     */
    String module();

    /**
     * 操作名，如“新增”
     */
    String name();

    /**
     * 是否记录请求参数
     */
    boolean saveParams() default true;

}
