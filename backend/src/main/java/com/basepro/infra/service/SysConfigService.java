package com.basepro.infra.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.common.BizException;
import com.basepro.common.PageResult;
import com.basepro.infra.dto.ConfigQuery;
import com.basepro.infra.entity.SysConfig;
import com.basepro.infra.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 参数配置。sys_config 是全局表，不参与租户隔离。
 */
@Service
@RequiredArgsConstructor
public class SysConfigService {

    private final SysConfigMapper configMapper;

    public PageResult<SysConfig> page(ConfigQuery query) {
        Page<SysConfig> page = configMapper.selectPage(query.toPage(), Wrappers.<SysConfig>lambdaQuery()
                .like(StringUtils.hasText(query.getName()), SysConfig::getName, query.getName())
                .like(StringUtils.hasText(query.getConfigKey()), SysConfig::getConfigKey, query.getConfigKey())
                .like(StringUtils.hasText(query.getCategory()), SysConfig::getCategory, query.getCategory())
                .eq(query.getType() != null, SysConfig::getType, query.getType())
                .ge(query.beginTime() != null, SysConfig::getCreateTime, query.beginTime())
                .le(query.endTime() != null, SysConfig::getCreateTime, query.endTime())
                .orderByDesc(SysConfig::getId));
        return PageResult.of(page);
    }

    public SysConfig get(Long id) {
        SysConfig config = configMapper.selectById(id);
        if (config == null) {
            throw new BizException("参数不存在");
        }
        return config;
    }

    /**
     * 按键名取参数值。不可见的参数不对外暴露，一律按“不存在”处理
     */
    public String getValueByKey(String configKey) {
        SysConfig config = configMapper.selectOne(Wrappers.<SysConfig>lambdaQuery()
                .eq(SysConfig::getConfigKey, configKey), false);
        if (config == null || !Boolean.TRUE.equals(config.getVisible())) {
            throw new BizException("参数不存在");
        }
        return config.getConfigValue();
    }

    public Long create(SysConfig config) {
        config.setId(null);
        validateKeyUnique(config);
        configMapper.insert(config);
        return config.getId();
    }

    public void update(SysConfig config) {
        SysConfig old = get(config.getId());
        // 内置参数的键名会被代码直接引用，改了就取不到值
        if (isBuiltIn(old) && !Objects.equals(old.getConfigKey(), config.getConfigKey())) {
            throw new BizException("系统内置参数的键名不允许修改");
        }
        validateKeyUnique(config);
        configMapper.updateById(config);
    }

    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<SysConfig> configs = configMapper.selectList(Wrappers.<SysConfig>lambdaQuery()
                .in(SysConfig::getId, ids));
        if (configs.stream().anyMatch(this::isBuiltIn)) {
            throw new BizException("系统内置参数不允许删除");
        }
        configMapper.deleteByIds(ids);
    }

    private boolean isBuiltIn(SysConfig config) {
        return Integer.valueOf(SysConfig.TYPE_BUILT_IN).equals(config.getType());
    }

    private void validateKeyUnique(SysConfig config) {
        Long count = configMapper.selectCount(Wrappers.<SysConfig>lambdaQuery()
                .eq(SysConfig::getConfigKey, config.getConfigKey())
                .ne(config.getId() != null, SysConfig::getId, config.getId()));
        if (count > 0) {
            throw new BizException("参数键名已存在");
        }
    }

}
