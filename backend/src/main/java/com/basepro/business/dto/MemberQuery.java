package com.basepro.business.dto;

import com.basepro.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemberQuery extends PageQuery {

    private String username;

    private String nickname;

    private String mobile;

    private Integer status;

    /** 头像审核状态：1 待审 */
    private Integer avatarAuditStatus;

}
