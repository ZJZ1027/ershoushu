package com.basepro.business;

public final class BookConstants {

    private BookConstants() {
    }

    public static final int BOOK_PENDING = 0;
    public static final int BOOK_ON_SALE = 1;
    public static final int BOOK_RESERVED = 2;
    public static final int BOOK_SOLD = 3;
    public static final int BOOK_OFF_SHELF = 4;
    public static final int BOOK_REJECTED = 5;
    /** 草稿：仅自己可见，未提交审核 */
    public static final int BOOK_DRAFT = 6;

    public static final int ORDER_PENDING = 0;
    public static final int ORDER_AGREED = 1;
    public static final int ORDER_COMPLETED = 2;
    public static final int ORDER_CANCELLED = 3;

    public static final int REPORT_PENDING = 0;
    public static final int REPORT_HANDLED = 1;
    public static final int REPORT_REJECTED = 2;

    public static final int TARGET_BOOK = 1;
    public static final int TARGET_USER = 2;

    public static final String ROLE_APP_USER = "app_user";

    /** 平台通知账号，用于向用户端留言推送审核/举报结果 */
    public static final String PLATFORM_USERNAME = "platform";

}
