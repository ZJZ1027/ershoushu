package com.basepro.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.common.PageResult;
import com.basepro.system.dto.OperLogQuery;
import com.basepro.system.entity.SysOperLog;
import com.basepro.system.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 操作日志。只提供查询，写入由 {@link com.basepro.system.log.OperLogAspect} 完成。
 */
@Service
@RequiredArgsConstructor
public class SysOperLogService {

    /**
     * 表示成功的结果码：切面记录的是响应体里的 code（200），列默认值 0 一并视为成功
     */
    private static final List<Integer> SUCCESS_CODES = List.of(0, 200);

    private final SysOperLogMapper operLogMapper;

    public PageResult<SysOperLog> page(OperLogQuery query) {
        Integer resultCode = query.getResultCode();
        // 传 0 看成功，传其它值看失败：失败的结果码种类很多，逐个枚举没有意义
        boolean onlySuccess = resultCode != null && resultCode == 0;
        boolean onlyFailure = resultCode != null && resultCode != 0;
        Page<SysOperLog> page = operLogMapper.selectPage(query.toPage(), Wrappers.<SysOperLog>lambdaQuery()
                .like(StringUtils.hasText(query.getModule()), SysOperLog::getModule, query.getModule())
                .like(StringUtils.hasText(query.getName()), SysOperLog::getName, query.getName())
                .like(StringUtils.hasText(query.getUsername()), SysOperLog::getUsername, query.getUsername())
                .in(onlySuccess, SysOperLog::getResultCode, SUCCESS_CODES)
                .notIn(onlyFailure, SysOperLog::getResultCode, SUCCESS_CODES)
                .ge(query.beginTime() != null, SysOperLog::getCreateTime, query.beginTime())
                .le(query.endTime() != null, SysOperLog::getCreateTime, query.endTime())
                .orderByDesc(SysOperLog::getId));
        return PageResult.of(page);
    }

    public static boolean isSuccess(Integer resultCode) {
        return resultCode == null || SUCCESS_CODES.contains(resultCode);
    }

}
