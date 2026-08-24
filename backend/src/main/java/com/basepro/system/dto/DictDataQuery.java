package com.basepro.system.dto;

import com.basepro.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DictDataQuery extends PageQuery {

    private String label;

    /**
     * 字典类型，精确匹配
     */
    private String dictType;

    private Integer status;

}
