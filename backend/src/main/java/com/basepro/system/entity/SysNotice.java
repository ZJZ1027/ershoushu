package com.basepro.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basepro.common.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
public class SysNotice extends BaseEntity {

    @TableId
    private Long id;

    @NotBlank(message = "公告标题不能为空")
    @Size(max = 50, message = "公告标题长度不能超过 50 个字符")
    private String title;

    private String content;

    private Integer type;

    private Integer status;

    private String remark;

}
