package com.basepro.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 刷新令牌。该表不做租户隔离（见 basepro.tenant.ignore-tables），因此 tenantId 需显式写入。
 */
@Data
@TableName("sys_token")
public class SysToken implements Serializable {

    @TableId
    private Long id;

    private Long tenantId;

    private Long userId;

    private String refreshToken;

    private LocalDateTime expiresTime;

    private LocalDateTime createTime;

}
