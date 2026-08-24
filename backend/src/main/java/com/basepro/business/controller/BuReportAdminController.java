package com.basepro.business.controller;

import com.basepro.business.dto.ReportQuery;
import com.basepro.business.entity.BuReport;
import com.basepro.business.service.BuReportService;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import com.basepro.system.log.OperLog;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "举报")
@RestController
@RequestMapping("/business/report")
@RequiredArgsConstructor
public class BuReportAdminController {

    private final BuReportService reportService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('business:report:query')")
    public R<PageResult<BuReport>> page(@Valid ReportQuery query) {
        return R.ok(reportService.page(query));
    }

    @PutMapping("/handle")
    @PreAuthorize("hasAuthority('business:report:handle')")
    @OperLog(module = "举报", name = "处理")
    public R<Void> handle(@RequestParam Long id,
                          @RequestParam boolean pass,
                          @RequestParam(required = false) String remark) {
        reportService.handle(id, pass, remark);
        return R.ok();
    }

}
