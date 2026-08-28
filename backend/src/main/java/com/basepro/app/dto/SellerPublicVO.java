package com.basepro.app.dto;

public record SellerPublicVO(
        Long id,
        String nickname,
        String avatar,
        String signature,
        String campus,
        Long onSaleCount,
        Long followerCount,
        Long followingCount,
        Boolean isFollowing
) {
}
