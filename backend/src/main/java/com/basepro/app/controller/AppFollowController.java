package com.basepro.app.controller;

import com.basepro.app.dto.UserFollowVO;
import com.basepro.app.service.AppFollowService;
import com.basepro.common.PageQuery;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户端关注")
@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class AppFollowController {

    private final AppFollowService followService;

    @PostMapping("/toggle")
    public R<Boolean> toggle(@RequestParam Long userId) {
        return R.ok(followService.toggleFollow(userId));
    }

    @GetMapping("/followers")
    public R<PageResult<UserFollowVO>> followers(@RequestParam Long userId, @Valid PageQuery query) {
        return R.ok(followService.followers(query, userId));
    }

    @GetMapping("/following")
    public R<PageResult<UserFollowVO>> following(@RequestParam Long userId, @Valid PageQuery query) {
        return R.ok(followService.following(query, userId));
    }
}
