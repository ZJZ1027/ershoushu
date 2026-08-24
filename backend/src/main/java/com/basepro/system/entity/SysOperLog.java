package com.basepro.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志。由 {@link com.basepro.system.log.OperLogAspect} 写入。
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog implements Serializable {

    @TableId
    private Long id;

    private String module;

    private String name;

    private Long userId;

    private String username;

    private String requestMethod;

    private String requestUrl;

    private String requestParams;

    private String javaMethod;

    private String userIp;

    private String userAgent;

    private Integer duration;

    private Integer resultCode;

    private String resultMsg;

    private LocalDateTime createTime;

}
