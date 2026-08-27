package com.basepro.business.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basepro.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bu_inquiry_msg")
public class BuInquiryMsg extends BaseEntity {

    @TableId
    private Long id;

    @JsonIgnore
    @TableField(value = "tenant_id", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Long tenantId;

    private Long inquiryId;

    private Long senderId;

    private String content;

    /** 是否已撤回：0否 1是 */
    private Integer recalled;

    @TableField(exist = false)
    private String senderNickname;

    /** 当前用户是否可撤回（服务端计算） */
    @TableField(exist = false)
    private Boolean canRecall;

}
