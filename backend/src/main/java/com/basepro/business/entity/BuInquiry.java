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
@TableName("bu_inquiry")
public class BuInquiry extends BaseEntity {

    @TableId
    private Long id;

    @JsonIgnore
    @TableField(value = "tenant_id", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Long tenantId;

    private Long bookId;

    private Long buyerId;

    private Long sellerId;

    private String lastMsg;

    private LocalDateTime lastTime;

    /** 买家未读：1 有新留言 */
    private Integer buyerUnread;

    /** 卖家未读：1 有新留言 */
    private Integer sellerUnread;

    /** 管理端未读：1 有新留言待抽查 */
    private Integer adminUnread;

    @TableField(exist = false)
    private String bookTitle;

    @TableField(exist = false)
    private String peerNickname;

    @TableField(exist = false)
    private String peerAvatar;

    /** 当前登录用户视角的未读：1 未读 */
    @TableField(exist = false)
    private Integer unread;

    /** 是否为平台系统通知会话 */
    @TableField(exist = false)
    private Boolean systemNotice;

}
