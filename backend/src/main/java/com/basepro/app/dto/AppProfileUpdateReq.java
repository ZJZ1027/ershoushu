package com.basepro.app.dto;

import jakarta.validation.constraints.Size;

public record AppProfileUpdateReq(@Size(max = 30) String nickname,
                                  @Size(max = 11) String mobile,
                                  @Size(max = 64) String wechat,
                                  @Size(max = 64) String campus,
                                  @Size(max = 100) String signature,
                                  Integer sex,
                                  String avatar) {
}
