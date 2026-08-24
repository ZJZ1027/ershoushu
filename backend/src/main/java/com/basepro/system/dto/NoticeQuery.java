package com.basepro.system.dto;

import com.basepro.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeQuery extends PageQuery {

    private String title;

    private Integer type;

    private Integer status;

}
