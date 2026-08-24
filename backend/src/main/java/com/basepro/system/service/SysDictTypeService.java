package com.basepro.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.common.BizException;
import com.basepro.common.PageResult;
import com.basepro.system.dto.DictTypeQuery;
import com.basepro.system.entity.SysDictData;
import com.basepro.system.entity.SysDictType;
import com.basepro.system.mapper.SysDictDataMapper;
import com.basepro.system.mapper.SysDictTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 字典类型。sys_dict_type 是全局表，不参与租户隔离。
 */
@Service
@RequiredArgsConstructor
public class SysDictTypeService {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;

    public PageResult<SysDictType> page(DictTypeQuery query) {
        Page<SysDictType> page = dictTypeMapper.selectPage(query.toPage(), Wrappers.<SysDictType>lambdaQuery()
                .like(StringUtils.hasText(query.getName()), SysDictType::getName, query.getName())
                .like(StringUtils.hasText(query.getType()), SysDictType::getType, query.getType())
                .eq(query.getStatus() != null, SysDictType::getStatus, query.getStatus())
                .ge(query.beginTime() != null, SysDictType::getCreateTime, query.beginTime())
                .le(query.endTime() != null, SysDictType::getCreateTime, query.endTime())
                .orderByDesc(SysDictType::getId));
        return PageResult.of(page);
    }

    public List<SysDictType> simpleList() {
        return dictTypeMapper.selectList(Wrappers.<SysDictType>lambdaQuery()
                .select(SysDictType::getId, SysDictType::getName, SysDictType::getType)
                .eq(SysDictType::getStatus, 0)
                .orderByAsc(SysDictType::getId));
    }

    public SysDictType get(Long id) {
        SysDictType dictType = dictTypeMapper.selectById(id);
        if (dictType == null) {
            throw new BizException("字典类型不存在");
        }
        return dictType;
    }

    public Long create(SysDictType dictType) {
        dictType.setId(null);
        validateUnique(dictType);
        dictTypeMapper.insert(dictType);
        return dictType.getId();
    }

    public void update(SysDictType dictType) {
        get(dictType.getId());
        validateUnique(dictType);
        dictTypeMapper.updateById(dictType);
    }

    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<SysDictType> dictTypes = dictTypeMapper.selectList(Wrappers.<SysDictType>lambdaQuery()
                .in(SysDictType::getId, ids));
        if (dictTypes.isEmpty()) {
            return;
        }
        // 字典类型下还有数据时不允许删除，避免留下取不到类型的孤儿数据
        List<String> types = dictTypes.stream().map(SysDictType::getType).toList();
        Long used = dictDataMapper.selectCount(Wrappers.<SysDictData>lambdaQuery()
                .in(SysDictData::getDictType, types));
        if (used > 0) {
            throw new BizException("字典类型下存在字典数据，无法删除");
        }
        dictTypeMapper.deleteByIds(ids);
    }

    private void validateUnique(SysDictType dictType) {
        Long count = dictTypeMapper.selectCount(Wrappers.<SysDictType>lambdaQuery()
                .eq(SysDictType::getName, dictType.getName())
                .ne(dictType.getId() != null, SysDictType::getId, dictType.getId()));
        if (count > 0) {
            throw new BizException("字典名称已存在");
        }
        count = dictTypeMapper.selectCount(Wrappers.<SysDictType>lambdaQuery()
                .eq(SysDictType::getType, dictType.getType())
                .ne(dictType.getId() != null, SysDictType::getId, dictType.getId()));
        if (count > 0) {
            throw new BizException("字典类型已存在");
        }
    }

}
