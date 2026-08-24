package com.basepro.business.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basepro.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bu_order")
public class BuBookOrder extends BaseEntity {

    @TableId
    private Long id;

    @JsonIgnore
    @TableField(value = "tenant_id", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Long tenantId;

    private String orderNo;

    private Long bookId;

    private Long buyerId;

    private Long sellerId;

    private Integer status;

    private LocalDateTime meetupTime;

    private String meetupPlace;

    private String remark;

    private String cancelReason;

    private Integer buyerConfirmed;

    private Integer sellerConfirmed;

    @TableField(exist = false)
    private String bookTitle;

    @TableField(exist = false)
    private String buyerNickname;

    @TableField(exist = false)
    private String sellerNickname;

    @TableField(exist = false)
    private String sellerMobile;

    @TableField(exist = false)
    private String sellerWechat;

    @TableField(exist = false)
    private String buyerMobile;

    @TableField(exist = false)
    private String buyerWechat;

}
