-- 平台通知账号：向用户端「留言」推送举报处理结果。停用状态，不用于登录。
INSERT INTO sys_user (tenant_id, username, password, nickname, remark, dept_id, sex, status, creator, updater)
SELECT 1, 'platform', '$2a$10$uFfmgGay7TKGr.2w2GdA7efj/w7BvV/tsRZ.i3N6x/taCMPHwz8lC', '平台通知',
       '系统通知账号，请勿登录', 100, 0, 1, 'system', 'system'
FROM (SELECT 1 AS x) dummy
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'platform' AND deleted = 0);
