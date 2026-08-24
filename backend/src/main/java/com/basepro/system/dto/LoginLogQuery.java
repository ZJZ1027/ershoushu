package com.basepro.system.dto;

import com.basepro.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoginLogQuery extends PageQuery {

    @Schema(description = "用户账号，模糊匹配")
    private String username;

    @Schema(description = "登录 IP，模糊匹配")
    private String userIp;

    /**
     * 登录结果筛选。前端下拉传 true / false，同时兼容 0 成功、1 失败的传法，
     * 因此用字符串接收，再由 {@link #success()} 归一化。
     */
    @Schema(description = "登录结果：true 或 0 成功，false 或 1 失败")
    private String status;

    /**
     * @return true 只看成功，false 只看失败，null 不筛选
     */
    public Boolean success() {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        return "true".equalsIgnoreCase(status) || "0".equals(status);
    }

}
