package com.basepro.system.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 个人中心信息：用户基本信息 + 所属部门、角色、岗位。
 */
public record ProfileVO(Long id,
                        String username,
                        String nickname,
                        Item dept,
                        List<Item> roles,
                        List<Item> posts,
                        String email,
                        String mobile,
                        Integer sex,
                        String avatar,
                        Integer status,
                        String remark,
                        String loginIp,
                        LocalDateTime loginDate,
                        LocalDateTime createTime) {

    /**
     * 部门、角色、岗位的展示项
     */
    public record Item(Long id, String name) {
    }

}
