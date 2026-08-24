package com.basepro.common;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类：审计字段由 {@link com.basepro.config.AuditMetaObjectHandler} 自动填充。
 * <p>
 * 逻辑删除由 MyBatis-Plus 全局配置 + {@link TableLogic} 处理，查询时自动追加 deleted = 0。
 */
@Data
public class BaseEntity implements Serializable {

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人（用户名）
     */
    @TableField(fill = FieldFill.INSERT)
    private String creator;

    /**
     * 更新人（用户名）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updater;

    @JsonIgnore
    @TableLogic
    @TableField(select = false, fill = FieldFill.INSERT)
    private Boolean deleted;

}
