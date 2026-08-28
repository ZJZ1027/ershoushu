package com.basepro.business.dto;

import com.basepro.business.entity.BuBook;

public class BookDetailVO extends BuBook {

    private Boolean favorited;

    private String sellerMobile;

    private String sellerWechat;

    private String sellerSignature;

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

    public String getSellerSignature() {
        return sellerSignature;
    }

    public void setSellerSignature(String sellerSignature) {
        this.sellerSignature = sellerSignature;
    }

}
