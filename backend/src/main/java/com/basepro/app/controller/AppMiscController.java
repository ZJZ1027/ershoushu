package com.basepro.app.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.basepro.app.dto.ReportCreateReq;
import com.basepro.business.service.BuReportService;
import com.basepro.common.R;
import com.basepro.infra.service.SysFileService;
import com.basepro.system.entity.SysDictData;
import com.basepro.system.entity.SysNotice;
import com.basepro.system.mapper.SysDictDataMapper;
import com.basepro.system.mapper.SysNoticeMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "用户端其它")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AppMiscController {

    private final BuReportService reportService;
    private final SysFileService fileService;
    private final SysNoticeMapper noticeMapper;
    private final SysDictDataMapper dictDataMapper;

    @PostMapping("/report/create")
    public R<Long> report(@Valid @RequestBody ReportCreateReq req) {
        return R.ok(reportService.create(req));
    }

    @PostMapping("/file/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        return R.ok(fileService.upload(file));
    }

    @GetMapping("/notice/list")
    public R<List<SysNotice>> notices() {
        return R.ok(noticeMapper.selectList(Wrappers.<SysNotice>lambdaQuery()
                .eq(SysNotice::getStatus, 0)
                .orderByDesc(SysNotice::getId)
                .last("LIMIT 10")));
    }

    @GetMapping("/dict/data")
    public R<List<SysDictData>> dict(@RequestParam String type) {
        return R.ok(dictDataMapper.selectList(Wrappers.<SysDictData>lambdaQuery()
                .eq(SysDictData::getDictType, type)
                .eq(SysDictData::getStatus, 0)
                .orderByAsc(SysDictData::getSort)));
    }

}
