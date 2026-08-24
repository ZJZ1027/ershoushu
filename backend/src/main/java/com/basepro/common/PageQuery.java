package com.basepro.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 分页查询参数基类，各模块的 Query 继承它再补充自己的筛选字段。
 */
@Data
public class PageQuery {

    @Schema(description = "页码，从 1 开始", example = "1")
    private Integer pageNo = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    /**
     * 创建时间范围，前端日期区间控件传两个值：createTime=开始&createTime=结束
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间区间")
    private LocalDateTime[] createTime;

    public <T> Page<T> toPage() {
        return Page.of(pageNo == null || pageNo < 1 ? 1 : pageNo,
                pageSize == null || pageSize < 1 ? 10 : pageSize);
    }

    public LocalDateTime beginTime() {
        return createTime != null && createTime.length > 0 ? createTime[0] : null;
    }

    public LocalDateTime endTime() {
        return createTime != null && createTime.length > 1 ? createTime[1] : null;
    }

}
