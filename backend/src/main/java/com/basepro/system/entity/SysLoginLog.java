package com.basepro.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志。只增不改，因此不继承 BaseEntity。
 */
@Data
@TableName("sys_login_log")
public class SysLoginLog implements Serializable {

    /**
     * 日志类型：登录
     */
    public static final int TYPE_LOGIN = 1;
    /**
     * 日志类型：登出
     */
    public static final int TYPE_LOGOUT = 2;

    /**
     * 登录成功
     */
    public static final int RESULT_SUCCESS = 0;
    /**
     * 账号或密码不正确
     */
    public static final int RESULT_BAD_CREDENTIALS = 1;
    /**
     * 账号被停用
     */
    public static final int RESULT_DISABLED = 2;

    @TableId
    private Long id;

    private Integer logType;

    private Long userId;

    private String username;

    private Integer result;

    private String userIp;

    private String userAgent;

    private LocalDateTime createTime;

}
