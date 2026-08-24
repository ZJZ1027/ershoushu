package com.basepro.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.common.BizException;
import com.basepro.common.PageResult;
import com.basepro.system.dto.PostQuery;
import com.basepro.system.entity.SysPost;
import com.basepro.system.entity.SysUserPost;
import com.basepro.system.mapper.SysPostMapper;
import com.basepro.system.mapper.SysUserPostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 岗位。其它模块可参照本类的写法：直接用 BaseMapper + LambdaQueryWrapper，不再包一层通用 Service。
 */
@Service
@RequiredArgsConstructor
public class SysPostService {

    private final SysPostMapper postMapper;
    private final SysUserPostMapper userPostMapper;

    public PageResult<SysPost> page(PostQuery query) {
        Page<SysPost> page = postMapper.selectPage(query.toPage(), Wrappers.<SysPost>lambdaQuery()
                .like(StringUtils.hasText(query.getCode()), SysPost::getCode, query.getCode())
                .like(StringUtils.hasText(query.getName()), SysPost::getName, query.getName())
                .eq(query.getStatus() != null, SysPost::getStatus, query.getStatus())
                .ge(query.beginTime() != null, SysPost::getCreateTime, query.beginTime())
                .le(query.endTime() != null, SysPost::getCreateTime, query.endTime())
                .orderByAsc(SysPost::getSort));
        return PageResult.of(page);
    }

    public List<SysPost> simpleList() {
        return postMapper.selectList(Wrappers.<SysPost>lambdaQuery()
                .select(SysPost::getId, SysPost::getName)
                .eq(SysPost::getStatus, 0)
                .orderByAsc(SysPost::getSort));
    }

    public SysPost get(Long id) {
        SysPost post = postMapper.selectById(id);
        if (post == null) {
            throw new BizException("岗位不存在");
        }
        return post;
    }

    public Long create(SysPost post) {
        post.setId(null);
        validateUnique(post);
        postMapper.insert(post);
        return post.getId();
    }

    public void update(SysPost post) {
        get(post.getId());
        validateUnique(post);
        postMapper.updateById(post);
    }

    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        Long used = userPostMapper.selectCount(Wrappers.<SysUserPost>lambdaQuery().in(SysUserPost::getPostId, ids));
        if (used > 0) {
            throw new BizException("岗位已分配给用户，无法删除");
        }
        postMapper.deleteByIds(ids);
    }

    private void validateUnique(SysPost post) {
        Long count = postMapper.selectCount(Wrappers.<SysPost>lambdaQuery()
                .eq(SysPost::getCode, post.getCode())
                .ne(post.getId() != null, SysPost::getId, post.getId()));
        if (count > 0) {
            throw new BizException("岗位编码已存在");
        }
        count = postMapper.selectCount(Wrappers.<SysPost>lambdaQuery()
                .eq(SysPost::getName, post.getName())
                .ne(post.getId() != null, SysPost::getId, post.getId()));
        if (count > 0) {
            throw new BizException("岗位名称已存在");
        }
    }

}
