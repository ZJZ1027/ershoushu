package com.basepro.app.dto;

import java.time.LocalDateTime;

public record AppProfileVO(Long id,
                           String username,
                           String nickname,
                           String mobile,
                           String wechat,
                           String campus,
                           String avatar,
                           Integer sex,
                           LocalDateTime createTime) {
}
