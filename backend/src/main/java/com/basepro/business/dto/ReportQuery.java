package com.basepro.business.dto;

import com.basepro.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReportQuery extends PageQuery {

    private Integer status;

    private Integer targetType;

}
