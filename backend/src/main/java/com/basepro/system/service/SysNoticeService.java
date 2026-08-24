package com.basepro.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.common.BizException;
import com.basepro.common.PageResult;
import com.basepro.system.dto.NoticeQuery;
import com.basepro.system.entity.SysNotice;
import com.basepro.system.mapper.SysNoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 通知公告。sys_notice 是租户表，tenant_id 由多租户插件自动处理。
 */
@Service
@RequiredArgsConstructor
public class SysNoticeService {

    private final SysNoticeMapper noticeMapper;

    public PageResult<SysNotice> page(NoticeQuery query) {
        Page<SysNotice> page = noticeMapper.selectPage(query.toPage(), Wrappers.<SysNotice>lambdaQuery()
                .like(StringUtils.hasText(query.getTitle()), SysNotice::getTitle, query.getTitle())
                .eq(query.getType() != null, SysNotice::getType, query.getType())
                .eq(query.getStatus() != null, SysNotice::getStatus, query.getStatus())
                .ge(query.beginTime() != null, SysNotice::getCreateTime, query.beginTime())
                .le(query.endTime() != null, SysNotice::getCreateTime, query.endTime())
                .orderByDesc(SysNotice::getId));
        return PageResult.of(page);
    }

    public SysNotice get(Long id) {
        SysNotice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BizException("公告不存在");
        }
        return notice;
    }

    public Long create(SysNotice notice) {
        notice.setId(null);
        noticeMapper.insert(notice);
        return notice.getId();
    }

    public void update(SysNotice notice) {
        get(notice.getId());
        noticeMapper.updateById(notice);
    }

    public void delete(Long id) {
        get(id);
        noticeMapper.deleteById(id);
    }

}
