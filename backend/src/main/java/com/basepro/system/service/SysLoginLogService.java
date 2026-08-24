package com.basepro.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.common.PageResult;
import com.basepro.system.dto.LoginLogQuery;
import com.basepro.system.entity.SysLoginLog;
import com.basepro.system.mapper.SysLoginLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 登录日志。只提供查询，写入在 {@link AuthService} 登录、登出时完成。
 */
@Service
@RequiredArgsConstructor
public class SysLoginLogService {

    private final SysLoginLogMapper loginLogMapper;

    public PageResult<SysLoginLog> page(LoginLogQuery query) {
        Boolean success = query.success();
        Page<SysLoginLog> page = loginLogMapper.selectPage(query.toPage(), Wrappers.<SysLoginLog>lambdaQuery()
                .like(StringUtils.hasText(query.getUsername()), SysLoginLog::getUsername, query.getUsername())
                .like(StringUtils.hasText(query.getUserIp()), SysLoginLog::getUserIp, query.getUserIp())
                .eq(Boolean.TRUE.equals(success), SysLoginLog::getResult, SysLoginLog.RESULT_SUCCESS)
                .ne(Boolean.FALSE.equals(success), SysLoginLog::getResult, SysLoginLog.RESULT_SUCCESS)
                .ge(query.beginTime() != null, SysLoginLog::getCreateTime, query.beginTime())
                .le(query.endTime() != null, SysLoginLog::getCreateTime, query.endTime())
                .orderByDesc(SysLoginLog::getId));
        return PageResult.of(page);
    }

}
