package com.basepro.app.controller;

import com.basepro.app.dto.SellerPublicVO;
import com.basepro.app.service.AppSellerService;
import com.basepro.common.R;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户端卖家")
@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
public class AppSellerController {

    private final AppSellerService sellerService;

    @GetMapping("/get")
    public R<SellerPublicVO> get(@RequestParam Long sellerId) {
        return R.ok(sellerService.getPublicProfile(sellerId));
    }
}
