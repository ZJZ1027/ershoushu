package com.basepro.system.dto;

import com.basepro.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TenantQuery extends PageQuery {

    @Schema(description = "租户名，模糊匹配")
    private String name;

    @Schema(description = "联系人，模糊匹配")
    private String contactName;

    @Schema(description = "联系手机，模糊匹配")
    private String contactMobile;

    @Schema(description = "状态：0 正常 1 停用")
    private Integer status;

}
