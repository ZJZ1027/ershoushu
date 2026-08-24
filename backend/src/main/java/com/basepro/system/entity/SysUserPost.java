package com.basepro.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basepro.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_post")
public class SysUserPost extends BaseEntity {

    @TableId
    private Long id;

    private Long userId;

    private Long postId;

    public static SysUserPost of(Long userId, Long postId) {
        SysUserPost entity = new SysUserPost();
        entity.setUserId(userId);
        entity.setPostId(postId);
        return entity;
    }

}
