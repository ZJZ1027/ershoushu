package com.basepro.business.controller;

import com.basepro.business.dto.AdminBadgeVO;
import com.basepro.business.dto.DashboardVO;
import com.basepro.business.dto.OrderQuery;
import com.basepro.business.entity.BuBookOrder;
import com.basepro.business.service.BuMemberService;
import com.basepro.business.service.BuOrderService;
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

@Tag(name = "预约单")
@RestController
@RequestMapping("/business")
@RequiredArgsConstructor
public class BuOrderAdminController {

    private final BuOrderService orderService;
    private final BuMemberService memberService;

    @GetMapping("/dashboard/get")
    @PreAuthorize("hasAuthority('business:dashboard:query')")
    public R<DashboardVO> dashboard() {
        return R.ok(memberService.dashboard());
    }

    @GetMapping("/dashboard/badges")
    public R<AdminBadgeVO> badges(@RequestParam(required = false) Long memberSince) {
        return R.ok(memberService.badges(memberSince));
    }

    @GetMapping("/order/page")
    @PreAuthorize("hasAuthority('business:order:query')")
    public R<PageResult<BuBookOrder>> page(@Valid OrderQuery query) {
        return R.ok(orderService.adminPage(query));
    }

    @GetMapping("/order/get")
    @PreAuthorize("hasAuthority('business:order:query')")
    public R<BuBookOrder> get(@RequestParam Long id) {
        return R.ok(orderService.get(id, true));
    }

    @PutMapping("/order/close")
    @PreAuthorize("hasAuthority('business:order:close')")
    @OperLog(module = "预约单", name = "关闭")
    public R<Void> close(@RequestParam Long id, @RequestParam(required = false) String reason) {
        orderService.adminClose(id, reason);
        return R.ok();
    }

}
