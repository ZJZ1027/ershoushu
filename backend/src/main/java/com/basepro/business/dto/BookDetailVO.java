package com.basepro.business.dto;

import com.basepro.business.entity.BuBook;

public class BookDetailVO extends BuBook {

    private Boolean favorited;

    private String sellerMobile;

    private String sellerWechat;

    public Boolean getFavorited() {
        return favorited;
    }

    public void setFavorited(Boolean favorited) {
        this.favorited = favorited;
    }

    public String getSellerMobile() {
        return sellerMobile;
    }

    public void setSellerMobile(String sellerMobile) {
        this.sellerMobile = sellerMobile;
    }

    public String getSellerWechat() {
        return sellerWechat;
    }

    public void setSellerWechat(String sellerWechat) {
        this.sellerWechat = sellerWechat;
    }

}
