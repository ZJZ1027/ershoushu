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
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    @TableId
    private Long id;

    private Integer sort;

    @NotBlank(message = "字典标签不能为空")
    @Size(max = 100, message = "字典标签长度不能超过 100 个字符")
    private String label;

    @NotBlank(message = "字典键值不能为空")
    @Size(max = 100, message = "字典键值长度不能超过 100 个字符")
    private String value;

    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    private Integer status;

    /**
     * 前端标签颜色：primary / success / warning / danger 等
     */
    private String colorType;

    private String cssClass;

    private String remark;

}
