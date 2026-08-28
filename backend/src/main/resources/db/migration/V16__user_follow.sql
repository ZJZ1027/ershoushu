-- 用户关注关系：follower 关注 followee
CREATE TABLE bu_user_follow
(
    id          bigint   NOT NULL AUTO_INCREMENT,
    tenant_id   bigint   NOT NULL DEFAULT 1,
    follower_id bigint   NOT NULL COMMENT '关注者',
    followee_id bigint   NOT NULL COMMENT '被关注者',
    creator     varchar(64)       DEFAULT NULL,
    create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)       DEFAULT NULL,
    update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     tinyint  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_follow (follower_id, followee_id, tenant_id),
    KEY idx_user_follow_followee (followee_id, tenant_id),
    KEY idx_user_follow_follower (follower_id, tenant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '用户关注';
