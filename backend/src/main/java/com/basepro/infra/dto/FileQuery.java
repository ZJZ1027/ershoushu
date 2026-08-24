package com.basepro.infra.dto;

import com.basepro.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileQuery extends PageQuery {

    @Schema(description = "文件名，模糊匹配")
    private String name;

    @Schema(description = "文件类型，模糊匹配")
    private String type;

    @Schema(description = "存储路径，模糊匹配")
    private String path;

}
