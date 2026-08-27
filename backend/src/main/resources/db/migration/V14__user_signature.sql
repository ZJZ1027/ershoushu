-- 用户个性签名
ALTER TABLE sys_user
    ADD COLUMN signature varchar(200) DEFAULT NULL COMMENT '个性签名' AFTER campus;
