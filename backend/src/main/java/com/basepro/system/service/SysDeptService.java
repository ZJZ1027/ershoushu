package com.basepro.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.basepro.common.BizException;
import com.basepro.system.dto.DeptQuery;
import com.basepro.system.entity.SysDept;
import com.basepro.system.entity.SysUser;
import com.basepro.system.mapper.SysDeptMapper;
import com.basepro.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 部门。列表一律返回扁平结构，由前端拼成树。
 */
@Service
@RequiredArgsConstructor
public class SysDeptService {

    /**
     * 父子链最大上溯层数，防止脏数据造成死循环
     */
    private static final int MAX_DEPTH = 64;

    private final SysDeptMapper deptMapper;
    private final SysUserMapper userMapper;

    public List<SysDept> simpleList() {
        return deptMapper.selectList(Wrappers.<SysDept>lambdaQuery()
                .select(SysDept::getId, SysDept::getName, SysDept::getParentId)
                .eq(SysDept::getStatus, 0)
                .orderByAsc(SysDept::getParentId, SysDept::getSort));
    }

    public List<SysDept> list(DeptQuery query) {
        return deptMapper.selectList(Wrappers.<SysDept>lambdaQuery()
                .like(StringUtils.hasText(query.getName()), SysDept::getName, query.getName())
                .eq(query.getStatus() != null, SysDept::getStatus, query.getStatus())
                .orderByAsc(SysDept::getParentId, SysDept::getSort));
    }

    public SysDept get(Long id) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BizException("部门不存在");
        }
        return dept;
    }

    public Long create(SysDept dept) {
        dept.setId(null);
        validateParent(null, dept.getParentId());
        validateNameUnique(dept);
        deptMapper.insert(dept);
        return dept.getId();
    }

    public void update(SysDept dept) {
        get(dept.getId());
        validateParent(dept.getId(), dept.getParentId());
        validateNameUnique(dept);
        deptMapper.updateById(dept);
    }

    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            SysDept dept = get(id);
            Long children = deptMapper.selectCount(Wrappers.<SysDept>lambdaQuery()
                    .eq(SysDept::getParentId, id));
            if (children > 0) {
                throw new BizException("部门存在下级部门，无法删除：" + dept.getName());
            }
            Long users = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                    .eq(SysUser::getDeptId, id));
            if (users > 0) {
                throw new BizException("部门下存在用户，无法删除：" + dept.getName());
            }
        }
        deptMapper.deleteByIds(ids);
    }

    /**
     * 部门编号 -> 部门名称，供用户列表填充部门名（避免在 SQL 里 join）
     */
    public Map<Long, String> nameMap() {
        List<SysDept> list = deptMapper.selectList(Wrappers.<SysDept>lambdaQuery()
                .select(SysDept::getId, SysDept::getName));
        Map<Long, String> result = new HashMap<>(list.size());
        list.forEach(dept -> result.put(dept.getId(), dept.getName()));
        return result;
    }

    /**
     * 自身 + 所有子孙部门的编号。用户列表按部门筛选时要连子部门一起查。
     */
    public List<Long> selfAndChildIds(Long deptId) {
        if (deptId == null) {
            return List.of();
        }
        Map<Long, List<Long>> childrenMap = new HashMap<>();
        deptMapper.selectList(Wrappers.<SysDept>lambdaQuery()
                        .select(SysDept::getId, SysDept::getParentId))
                .forEach(dept -> childrenMap
                        .computeIfAbsent(dept.getParentId(), key -> new ArrayList<>())
                        .add(dept.getId()));
        Set<Long> result = new LinkedHashSet<>();
        Deque<Long> pending = new ArrayDeque<>();
        pending.add(deptId);
        while (!pending.isEmpty()) {
            Long current = pending.poll();
            // 已收集过说明数据成环，直接跳过
            if (!result.add(current)) {
                continue;
            }
            pending.addAll(childrenMap.getOrDefault(current, List.of()));
        }
        return new ArrayList<>(result);
    }

    /**
     * 父部门校验：必须存在，且不能是自己或自己的子孙部门
     */
    private void validateParent(Long id, Long parentId) {
        if (parentId == null) {
            throw new BizException("上级部门不能为空");
        }
        if (SysDept.ROOT_PARENT_ID == parentId) {
            return;
        }
        if (parentId.equals(id)) {
            throw new BizException("上级部门不能是自己");
        }
        SysDept parent = deptMapper.selectById(parentId);
        if (parent == null) {
            throw new BizException("上级部门不存在");
        }
        if (id == null) {
            return;
        }
        // 沿父级链上溯，若碰到自己，说明选中的父部门在自己的子树里
        Long cursor = parent.getParentId();
        for (int depth = 0; depth < MAX_DEPTH; depth++) {
            if (cursor == null || SysDept.ROOT_PARENT_ID == cursor) {
                return;
            }
            if (cursor.equals(id)) {
                throw new BizException("上级部门不能是自己的下级部门");
            }
            SysDept node = deptMapper.selectById(cursor);
            if (node == null) {
                return;
            }
            cursor = node.getParentId();
        }
    }

    /**
     * 同一父部门下部门名称唯一
     */
    private void validateNameUnique(SysDept dept) {
        Long count = deptMapper.selectCount(Wrappers.<SysDept>lambdaQuery()
                .eq(SysDept::getParentId, dept.getParentId())
                .eq(SysDept::getName, dept.getName())
                .ne(dept.getId() != null, SysDept::getId, dept.getId()));
        if (count > 0) {
            throw new BizException("同一上级部门下已存在该部门名称");
        }
    }

}
