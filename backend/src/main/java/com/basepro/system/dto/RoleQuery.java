package com.basepro.system.dto;

import com.basepro.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQuery extends PageQuery {

    @Schema(description = "角色名称，模糊匹配")
    private String name;

    @Schema(description = "角色标识，模糊匹配")
    private String code;

    @Schema(description = "状态：0 正常 1 停用")
    private Integer status;

}
