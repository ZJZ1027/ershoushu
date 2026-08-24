package com.basepro.business.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basepro.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bu_book")
public class BuBook extends BaseEntity {

    @TableId
    private Long id;

    @JsonIgnore
    @TableField(value = "tenant_id", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Long tenantId;

    private Long sellerId;

    private Long categoryId;

    @NotBlank(message = "书名不能为空")
    private String title;

    private String author;

    private String isbn;

    private String publisher;

    private String courseName;

    private String majorName;

    private String conditionCode;

    private BigDecimal originPrice;

    private BigDecimal price;

    private String campus;

    private String meetupPlace;

    private String description;

    private String coverUrl;

    private Integer status;

    private String rejectReason;

    @TableField(exist = false)
    private List<String> imageUrls;

    @TableField(exist = false)
    private String categoryName;

    @TableField(exist = false)
    private String sellerNickname;

}
