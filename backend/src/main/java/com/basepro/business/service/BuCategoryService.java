package com.basepro.business.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.business.dto.CategoryQuery;
import com.basepro.business.entity.BuCategory;
import com.basepro.business.mapper.BuCategoryMapper;
import com.basepro.common.BizException;
import com.basepro.common.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuCategoryService {

    private final BuCategoryMapper categoryMapper;

    public PageResult<BuCategory> page(CategoryQuery query) {
        Page<BuCategory> page = categoryMapper.selectPage(query.toPage(), Wrappers.<BuCategory>lambdaQuery()
                .like(StringUtils.hasText(query.getName()), BuCategory::getName, query.getName())
                .eq(query.getStatus() != null, BuCategory::getStatus, query.getStatus())
                .orderByAsc(BuCategory::getSort)
                .orderByDesc(BuCategory::getId));
        return PageResult.of(page);
    }

    public List<BuCategory> listEnabled() {
        return categoryMapper.selectList(Wrappers.<BuCategory>lambdaQuery()
                .eq(BuCategory::getStatus, 0)
                .orderByAsc(BuCategory::getSort)
                .orderByDesc(BuCategory::getId));
    }

    public BuCategory get(Long id) {
        BuCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BizException("分类不存在");
        }
        return category;
    }

    public Long create(BuCategory entity) {
        entity.setId(null);
        if (entity.getStatus() == null) {
            entity.setStatus(0);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        categoryMapper.insert(entity);
        return entity.getId();
    }

    public void update(BuCategory entity) {
        get(entity.getId());
        categoryMapper.updateById(entity);
    }

    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        categoryMapper.deleteByIds(ids);
    }

}
