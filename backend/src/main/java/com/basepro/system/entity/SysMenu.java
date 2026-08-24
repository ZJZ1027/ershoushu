package com.basepro.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basepro.common.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    /**
     * 目录
     */
    public static final int TYPE_DIR = 1;
    /**
     * 菜单
     */
    public static final int TYPE_MENU = 2;
    /**
     * 按钮
     */
    public static final int TYPE_BUTTON = 3;

    public static final long ROOT_PARENT_ID = 0L;

    @TableId
    private Long id;

    @NotBlank(message = "菜单名称不能为空")
    private String name;

    private String permission;

    @NotNull(message = "菜单类型不能为空")
    private Integer type;

    private Integer sort;

    private Long parentId;

    private String path;

    private String icon;

    private String component;

    private String componentName;

    private Integer status;

    private Boolean visible;

    private Boolean keepAlive;

    private Boolean alwaysShow;

    /**
     * 子菜单，构建菜单树时填充
     */
    @TableField(exist = false)
    private List<SysMenu> children;

}
