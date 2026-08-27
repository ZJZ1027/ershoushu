-- 用户端头像需管理端审核：当前生效头像仍用 avatar，待审/驳回用新字段
ALTER TABLE sys_user
    ADD COLUMN avatar_pending varchar(512) DEFAULT NULL COMMENT '待审核头像地址' AFTER avatar,
    ADD COLUMN avatar_audit_status tinyint NOT NULL DEFAULT 0 COMMENT '头像审核：0无待审 1待审 2已驳回' AFTER avatar_pending,
    ADD COLUMN avatar_reject_reason varchar(500) DEFAULT NULL COMMENT '头像驳回原因' AFTER avatar_audit_status;

INSERT INTO sys_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                      status, visible, keep_alive, always_show, creator, updater)
VALUES
(3053, '头像审核', 'business:member:avatar', 3, 3, 305, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system');
