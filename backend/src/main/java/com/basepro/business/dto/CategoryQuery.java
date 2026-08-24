package com.basepro.business.dto;

import com.basepro.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryQuery extends PageQuery {

    private String name;

    private Integer status;

}
