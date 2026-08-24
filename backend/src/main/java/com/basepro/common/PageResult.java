package com.basepro.common;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.function.Function;

/**
 * 分页结果。字段名与前端表格约定一致（list / total）。
 */
public record PageResult<T>(List<T> list, long total) {

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    /**
     * 分页结果转换，用于 Entity -> VO
     */
    public static <S, T> PageResult<T> of(IPage<S> page, Function<S, T> mapper) {
        return new PageResult<>(page.getRecords().stream().map(mapper).toList(), page.getTotal());
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(List.of(), 0L);
    }

}
