package com.basepro.business.controller;

import com.basepro.business.dto.MemberQuery;
import com.basepro.business.service.BuMemberService;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import com.basepro.system.dto.UpdateStatusReq;
import com.basepro.system.entity.SysUser;
import com.basepro.system.log.OperLog;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "会员")
@RestController
@RequestMapping("/business/member")
@RequiredArgsConstructor
public class BuMemberController {

    private final BuMemberService memberService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('business:member:query')")
    public R<PageResult<SysUser>> page(@Valid MemberQuery query) {
        return R.ok(memberService.page(query));
    }

    @PutMapping("/update-status")
    @PreAuthorize("hasAuthority('business:member:update')")
    @OperLog(module = "会员", name = "修改状态")
    public R<Void> updateStatus(@Valid @RequestBody UpdateStatusReq request) {
        memberService.updateStatus(request.id(), request.status());
        return R.ok();
    }

}
