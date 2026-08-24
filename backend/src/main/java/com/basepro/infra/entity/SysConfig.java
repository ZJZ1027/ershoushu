package com.basepro.infra.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basepro.common.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {

    /**
     * 系统内置参数，不允许删除
     */
    public static final int TYPE_BUILT_IN = 1;

    @TableId
    private Long id;

    @NotBlank(message = "参数分组不能为空")
    private String category;

    @NotBlank(message = "参数名称不能为空")
    @Size(max = 100, message = "参数名称长度不能超过 100 个字符")
    private String name;

    /**
     * 字段名与列名保持一致（config_key / config_value）：
     * key、value 是 MySQL 保留字，若属性叫 key，MyBatis-Plus 会生成 {@code config_key AS key} 这种非法别名
     */
    @NotBlank(message = "参数键名不能为空")
    @Size(max = 100, message = "参数键名长度不能超过 100 个字符")
    private String configKey;

    @NotBlank(message = "参数键值不能为空")
    private String configValue;

    private Integer type;

    private Boolean visible;

    private String remark;

}
