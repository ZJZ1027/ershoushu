package com.basepro.system.controller;

import com.basepro.common.PageResult;
import com.basepro.common.R;
import com.basepro.system.dto.NoticeQuery;
import com.basepro.system.entity.SysNotice;
import com.basepro.system.log.OperLog;
import com.basepro.system.service.SysNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "通知公告")
@RestController
@RequestMapping("/system/notice")
@RequiredArgsConstructor
public class SysNoticeController {

    private final SysNoticeService noticeService;

    @Operation(summary = "公告分页")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:notice:query')")
    public R<PageResult<SysNotice>> page(@Valid NoticeQuery query) {
        return R.ok(noticeService.page(query));
    }

    @Operation(summary = "公告详情")
    @GetMapping("/get")
    @PreAuthorize("hasAuthority('system:notice:query')")
    public R<SysNotice> get(@RequestParam("id") Long id) {
        return R.ok(noticeService.get(id));
    }

    @Operation(summary = "新增公告")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('system:notice:create')")
    @OperLog(module = "通知公告", name = "新增")
    public R<Long> create(@Valid @RequestBody SysNotice notice) {
        return R.ok(noticeService.create(notice));
    }

    @Operation(summary = "修改公告")
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('system:notice:update')")
    @OperLog(module = "通知公告", name = "修改")
    public R<Void> update(@Valid @RequestBody SysNotice notice) {
        noticeService.update(notice);
        return R.ok();
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('system:notice:delete')")
    @OperLog(module = "通知公告", name = "删除")
    public R<Void> delete(@RequestParam("id") Long id) {
        noticeService.delete(id);
        return R.ok();
    }

}
