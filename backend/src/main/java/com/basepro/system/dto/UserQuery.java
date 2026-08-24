package com.basepro.system.dto;

import com.basepro.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends PageQuery {

    private String username;

    private String mobile;

    private Integer status;

    @Schema(description = "部门编号，查询时包含其所有子部门")
    private Long deptId;

}
