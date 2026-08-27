-- 留言消息支持限时撤回（参考微信约 2 分钟）
ALTER TABLE bu_inquiry_msg
    ADD COLUMN recalled tinyint NOT NULL DEFAULT 0 COMMENT '是否已撤回：0否 1是' AFTER content;
