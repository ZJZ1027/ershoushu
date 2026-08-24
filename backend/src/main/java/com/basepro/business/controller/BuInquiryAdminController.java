package com.basepro.business.controller;

import com.basepro.business.entity.BuInquiry;
import com.basepro.business.entity.BuInquiryMsg;
import com.basepro.business.service.BuInquiryService;
import com.basepro.common.PageQuery;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "留言抽查")
@RestController
@RequestMapping("/business/inquiry")
@RequiredArgsConstructor
public class BuInquiryAdminController {

    private final BuInquiryService inquiryService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('business:inquiry:query')")
    public R<PageResult<BuInquiry>> page(@Valid PageQuery query) {
        return R.ok(inquiryService.adminPage(query));
    }

    @GetMapping("/messages")
    @PreAuthorize("hasAuthority('business:inquiry:query')")
    public R<List<BuInquiryMsg>> messages(@RequestParam Long inquiryId) {
        return R.ok(inquiryService.messages(inquiryId, true));
    }

}
