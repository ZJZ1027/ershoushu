package com.basepro.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.common.BizException;
import com.basepro.common.PageResult;
import com.basepro.system.dto.DictDataQuery;
import com.basepro.system.entity.SysDictData;
import com.basepro.system.entity.SysDictType;
import com.basepro.system.mapper.SysDictDataMapper;
import com.basepro.system.mapper.SysDictTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 字典数据。sys_dict_data 是全局表，不参与租户隔离。
 */
@Service
@RequiredArgsConstructor
public class SysDictDataService {

    private final SysDictDataMapper dictDataMapper;
    private final SysDictTypeMapper dictTypeMapper;

    public PageResult<SysDictData> page(DictDataQuery query) {
        Page<SysDictData> page = dictDataMapper.selectPage(query.toPage(), Wrappers.<SysDictData>lambdaQuery()
                .like(StringUtils.hasText(query.getLabel()), SysDictData::getLabel, query.getLabel())
                .eq(StringUtils.hasText(query.getDictType()), SysDictData::getDictType, query.getDictType())
                .eq(query.getStatus() != null, SysDictData::getStatus, query.getStatus())
                .ge(query.beginTime() != null, SysDictData::getCreateTime, query.beginTime())
                .le(query.endTime() != null, SysDictData::getCreateTime, query.endTime())
                .orderByAsc(SysDictData::getDictType)
                .orderByAsc(SysDictData::getSort)
                .orderByAsc(SysDictData::getId));
        return PageResult.of(page);
    }

    /**
     * 全量开启状态的字典数据，前端启动时拉一次做本地字典缓存，按 dictType 分组
     */
    public List<SysDictData> simpleList() {
        return dictDataMapper.selectList(Wrappers.<SysDictData>lambdaQuery()
                .select(SysDictData::getId, SysDictData::getLabel, SysDictData::getValue,
                        SysDictData::getDictType, SysDictData::getColorType, SysDictData::getCssClass)
                .eq(SysDictData::getStatus, 0)
                .orderByAsc(SysDictData::getDictType)
                .orderByAsc(SysDictData::getSort)
                .orderByAsc(SysDictData::getId));
    }

    /**
     * 某个字典类型下的全部字典数据（含停用）
     */
    public List<SysDictData> listByType(String dictType) {
        return dictDataMapper.selectList(Wrappers.<SysDictData>lambdaQuery()
                .eq(SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getSort)
                .orderByAsc(SysDictData::getId));
    }

    public SysDictData get(Long id) {
        SysDictData dictData = dictDataMapper.selectById(id);
        if (dictData == null) {
            throw new BizException("字典数据不存在");
        }
        return dictData;
    }

    public Long create(SysDictData dictData) {
        dictData.setId(null);
        validateDictType(dictData.getDictType());
        validateUnique(dictData);
        dictDataMapper.insert(dictData);
        return dictData.getId();
    }

    public void update(SysDictData dictData) {
        get(dictData.getId());
        validateDictType(dictData.getDictType());
        validateUnique(dictData);
        dictDataMapper.updateById(dictData);
    }

    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        dictDataMapper.deleteByIds(ids);
    }

    private void validateDictType(String dictType) {
        Long count = dictTypeMapper.selectCount(Wrappers.<SysDictType>lambdaQuery()
                .eq(SysDictType::getType, dictType));
        if (count == 0) {
            throw new BizException("字典类型不存在");
        }
    }

    /**
     * 同一字典类型下，键值与标签都不允许重复
     */
    private void validateUnique(SysDictData dictData) {
        Long count = dictDataMapper.selectCount(Wrappers.<SysDictData>lambdaQuery()
                .eq(SysDictData::getDictType, dictData.getDictType())
                .eq(SysDictData::getValue, dictData.getValue())
                .ne(dictData.getId() != null, SysDictData::getId, dictData.getId()));
        if (count > 0) {
            throw new BizException("字典键值已存在");
        }
        count = dictDataMapper.selectCount(Wrappers.<SysDictData>lambdaQuery()
                .eq(SysDictData::getDictType, dictData.getDictType())
                .eq(SysDictData::getLabel, dictData.getLabel())
                .ne(dictData.getId() != null, SysDictData::getId, dictData.getId()));
        if (count > 0) {
            throw new BizException("字典标签已存在");
        }
    }

}
