package com.basepro.business.dto;

import com.basepro.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class BookQuery extends PageQuery {

    private String title;

    private Long categoryId;

    private Integer status;

    private Long sellerId;

    private String campus;

    private String conditionCode;

    private String keyword;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

}
