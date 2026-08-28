package com.basepro.app.dto;

public record UserFollowVO(
        Long id,
        String nickname,
        String avatar,
        String signature,
        String campus,
        Boolean isFollowing
) {
}
