package com.basepro.system.dto;

import com.basepro.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DictTypeQuery extends PageQuery {

    private String name;

    private String type;

    private Integer status;

}
