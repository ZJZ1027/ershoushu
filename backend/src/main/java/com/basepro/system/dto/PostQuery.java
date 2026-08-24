package com.basepro.system.dto;

import com.basepro.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostQuery extends PageQuery {

    private String code;

    private String name;

    private Integer status;

}
