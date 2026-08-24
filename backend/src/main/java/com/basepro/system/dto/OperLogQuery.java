package com.basepro.system.dto;

import com.basepro.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OperLogQuery extends PageQuery {

    @Schema(description = "模块名，模糊匹配")
    private String module;

    @Schema(description = "操作名，模糊匹配")
    private String name;

    @Schema(description = "操作人账号，模糊匹配")
    private String username;

    /**
     * 结果筛选：传 0 只看成功，传其它值只看失败，不传则全部
     */
    @Schema(description = "结果码：0 成功，非 0 失败")
    private Integer resultCode;

}
