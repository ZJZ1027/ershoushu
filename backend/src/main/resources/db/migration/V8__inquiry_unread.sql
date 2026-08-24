-- 会话未读：对方发来新留言后置 1，点开会话后清 0
ALTER TABLE bu_inquiry
    ADD COLUMN buyer_unread tinyint NOT NULL DEFAULT 0 COMMENT '买家未读' AFTER last_time,
    ADD COLUMN seller_unread tinyint NOT NULL DEFAULT 0 COMMENT '卖家未读' AFTER buyer_unread;
