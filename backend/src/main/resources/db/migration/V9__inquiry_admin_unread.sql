-- 管理端留言抽查未读：用户新发/回复后置 1，管理员点开会话后清 0
ALTER TABLE bu_inquiry
    ADD COLUMN admin_unread tinyint NOT NULL DEFAULT 0 COMMENT '管理端未读' AFTER seller_unread;
