package com.basepro.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 菜单列表查询参数。菜单不分页，后端返回扁平列表，由前端自行构建树。
 */
@Data
public class MenuQuery {

    @Schema(description = "菜单名称，模糊匹配")
    private String name;

    @Schema(description = "状态：0 正常 1 停用")
    private Integer status;

}
