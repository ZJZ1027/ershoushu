package com.basepro.business.dto;

import com.basepro.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderQuery extends PageQuery {

    private String orderNo;

    private Integer status;

    private Long bookId;

    private Long buyerId;

    private Long sellerId;

}
