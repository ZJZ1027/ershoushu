package com.basepro.business.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.app.dto.ReportCreateReq;
import com.basepro.business.BookConstants;
import com.basepro.business.dto.ReportQuery;
import com.basepro.business.entity.BuBook;
import com.basepro.business.entity.BuReport;
import com.basepro.business.mapper.BuBookMapper;
import com.basepro.business.mapper.BuReportMapper;
import com.basepro.common.BizException;
import com.basepro.common.PageResult;
import com.basepro.security.SecurityUtils;
import com.basepro.system.entity.SysUser;
import com.basepro.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuReportService {

    private final BuReportMapper reportMapper;
    private final SysUserMapper userMapper;
    private final BuBookMapper bookMapper;
    private final BuInquiryService inquiryService;

    public PageResult<BuReport> page(ReportQuery query) {
        Page<BuReport> page = reportMapper.selectPage(query.toPage(), Wrappers.<BuReport>lambdaQuery()
                .eq(query.getStatus() != null, BuReport::getStatus, query.getStatus())
                .eq(query.getTargetType() != null, BuReport::getTargetType, query.getTargetType())
                .orderByDesc(BuReport::getId));
        fill(page.getRecords());
        return PageResult.of(page);
    }

    public Long create(ReportCreateReq req) {
        BuReport report = new BuReport();
        report.setReporterId(SecurityUtils.getUserId());
        report.setTargetType(req.targetType());
        report.setTargetId(req.targetId());
        report.setReasonCode(req.reasonCode());
        report.setContent(req.content());
        report.setStatus(BookConstants.REPORT_PENDING);
        reportMapper.insert(report);
        return report.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void handle(Long id, boolean pass, String remark) {
        BuReport exist = reportMapper.selectById(id);
        if (exist == null) {
            throw new BizException("举报不存在");
        }
        if (!Objects.equals(exist.getStatus(), BookConstants.REPORT_PENDING)) {
            throw new BizException("该举报已处理");
        }
        BuReport update = new BuReport();
        update.setId(id);
        update.setStatus(pass ? BookConstants.REPORT_HANDLED : BookConstants.REPORT_REJECTED);
        update.setHandleRemark(remark);
        reportMapper.updateById(update);
        notifyReporter(exist, pass, remark);
    }

    private void notifyReporter(BuReport report, boolean pass, String remark) {
        if (!Objects.equals(report.getTargetType(), BookConstants.TARGET_BOOK)) {
            return;
        }
        BuBook book = bookMapper.selectById(report.getTargetId());
        String title = book == null || !StringUtils.hasText(book.getTitle()) ? "相关书籍" : book.getTitle();
        StringBuilder text = new StringBuilder();
        if (pass) {
            text.append("您举报的《").append(title).append("》已处理。");
        } else {
            text.append("您举报的《").append(title).append("》未予受理。");
        }
        if (StringUtils.hasText(remark)) {
            text.append(pass ? "处理说明：" : "原因：").append(remark.trim());
        }
        inquiryService.notifyUser(report.getReporterId(), report.getTargetId(), text.toString());
    }

    private void fill(List<BuReport> list) {
        Set<Long> ids = list.stream().map(BuReport::getReporterId).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, SysUser> users = userMapper.selectByIds(ids).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        for (BuReport report : list) {
            SysUser user = users.get(report.getReporterId());
            report.setReporterNickname(user == null ? null : user.getNickname());
        }
    }

}
