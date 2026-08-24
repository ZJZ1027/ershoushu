-- ----------------------------------------------------------------------------
-- 初始化数据。约定：
--   1. 显式写 id，方便后续迁移脚本引用；菜单 id 段位：目录 1~99、系统管理菜单 100~199、
--      基础设施菜单 200~299、按钮 1000+（按钮 id = 所属菜单 id * 10 + 序号，便于对照）；
--   2. 超级管理员（super_admin）在代码里直接放行全部权限，因此不写 sys_role_menu；
--   3. 初始账号 admin / admin123。
-- ----------------------------------------------------------------------------

-- ---------------------------- 租户 ----------------------------
INSERT INTO sys_tenant (id, name, contact_name, contact_mobile, status, account_count, creator, updater)
VALUES (1, '默认租户', '管理员', '15888888888', 0, 0, 'system', 'system');

-- ---------------------------- 部门 / 岗位 ----------------------------
INSERT INTO sys_dept (id, tenant_id, name, parent_id, sort, leader_user_id, phone, email, status, creator, updater)
VALUES (100, 1, '总公司', 0, 0, 1, '15888888888', 'admin@basepro.com', 0, 'system', 'system'),
       (101, 1, '研发部', 100, 1, 1, NULL, NULL, 0, 'system', 'system'),
       (102, 1, '市场部', 100, 2, NULL, NULL, NULL, 0, 'system', 'system'),
       (103, 1, '财务部', 100, 3, NULL, NULL, NULL, 0, 'system', 'system');

INSERT INTO sys_post (id, tenant_id, code, name, sort, status, remark, creator, updater)
VALUES (1, 1, 'ceo', '董事长', 1, 0, NULL, 'system', 'system'),
       (2, 1, 'se', '项目经理', 2, 0, NULL, 'system', 'system'),
       (3, 1, 'hr', '人力资源', 3, 0, NULL, 'system', 'system'),
       (4, 1, 'user', '普通员工', 4, 0, NULL, 'system', 'system');

-- ---------------------------- 用户 / 角色 ----------------------------
-- 密码为 admin123，BCrypt（强度 10）
INSERT INTO sys_user (id, tenant_id, username, password, nickname, remark, dept_id, email, mobile, sex, status,
                      creator, updater)
VALUES (1, 1, 'admin', '$2a$10$uFfmgGay7TKGr.2w2GdA7efj/w7BvV/tsRZ.i3N6x/taCMPHwz8lC', '管理员', '初始管理员账号',
        100, 'admin@basepro.com', '15888888888', 1, 0, 'system', 'system');

INSERT INTO sys_role (id, tenant_id, name, code, sort, status, type, remark, creator, updater)
VALUES (1, 1, '超级管理员', 'super_admin', 1, 0, 1, '拥有全部权限，无需分配菜单', 'system', 'system'),
       (2, 1, '普通角色', 'common', 2, 0, 2, '示例角色，请按需分配菜单权限', 'system', 'system');

INSERT INTO sys_user_role (id, tenant_id, user_id, role_id, creator, updater)
VALUES (1, 1, 1, 1, 'system', 'system');

INSERT INTO sys_user_post (id, tenant_id, user_id, post_id, creator, updater)
VALUES (1, 1, 1, 1, 'system', 'system');

-- ---------------------------- 菜单 ----------------------------
INSERT INTO sys_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                      status, visible, keep_alive, always_show, creator, updater)
VALUES
-- 一级目录
(1, '系统管理', '', 1, 10, 0, '/system', 'ep:tools', NULL, NULL, 0, 1, 1, 1, 'system', 'system'),
(2, '基础设施', '', 1, 20, 0, '/infra', 'ep:monitor', NULL, NULL, 0, 1, 1, 1, 'system', 'system'),
(3, '审计日志', '', 1, 80, 1, 'log', 'ep:document', NULL, NULL, 0, 1, 1, 1, 'system', 'system'),

-- 系统管理
(100, '用户管理', '', 2, 1, 1, 'user', 'ep:avatar', 'system/user/index', 'SystemUser', 0, 1, 1, 1, 'system', 'system'),
(101, '角色管理', '', 2, 2, 1, 'role', 'ep:coordinate', 'system/role/index', 'SystemRole', 0, 1, 1, 1, 'system', 'system'),
(102, '菜单管理', '', 2, 3, 1, 'menu', 'ep:menu', 'system/menu/index', 'SystemMenu', 0, 1, 1, 1, 'system', 'system'),
(103, '部门管理', '', 2, 4, 1, 'dept', 'ep:office-building', 'system/dept/index', 'SystemDept', 0, 1, 1, 1, 'system', 'system'),
(104, '岗位管理', '', 2, 5, 1, 'post', 'ep:briefcase', 'system/post/index', 'SystemPost', 0, 1, 1, 1, 'system', 'system'),
(105, '字典管理', '', 2, 6, 1, 'dict', 'ep:collection', 'system/dict/index', 'SystemDict', 0, 1, 1, 1, 'system', 'system'),
(106, '通知公告', '', 2, 7, 1, 'notice', 'ep:chat-line-square', 'system/notice/index', 'SystemNotice', 0, 1, 1, 1, 'system', 'system'),
(107, '操作日志', '', 2, 1, 3, 'operate-log', 'ep:document-copy', 'system/operatelog/index', 'SystemOperateLog', 0, 1, 1, 1, 'system', 'system'),
(108, '登录日志', '', 2, 2, 3, 'login-log', 'ep:key', 'system/loginlog/index', 'SystemLoginLog', 0, 1, 1, 1, 'system', 'system'),
(109, '租户管理', '', 2, 90, 1, 'tenant', 'ep:house', 'system/tenant/index', 'SystemTenant', 0, 1, 1, 1, 'system', 'system'),

-- 基础设施
(200, '参数配置', '', 2, 1, 2, 'config', 'ep:setting', 'infra/config/index', 'InfraConfig', 0, 1, 1, 1, 'system', 'system'),
(201, '文件管理', '', 2, 2, 2, 'file', 'ep:files', 'infra/file/index', 'InfraFile', 0, 1, 1, 1, 'system', 'system'),

-- 用户管理按钮
(1001, '用户查询', 'system:user:query', 3, 1, 100, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1002, '用户新增', 'system:user:create', 3, 2, 100, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1003, '用户修改', 'system:user:update', 3, 3, 100, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1004, '用户删除', 'system:user:delete', 3, 4, 100, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1005, '用户导出', 'system:user:export', 3, 5, 100, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1006, '用户导入', 'system:user:import', 3, 6, 100, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1007, '重置密码', 'system:user:update-password', 3, 7, 100, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1008, '分配角色', 'system:permission:assign-user-role', 3, 8, 100, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),

-- 角色管理按钮
(1011, '角色查询', 'system:role:query', 3, 1, 101, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1012, '角色新增', 'system:role:create', 3, 2, 101, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1013, '角色修改', 'system:role:update', 3, 3, 101, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1014, '角色删除', 'system:role:delete', 3, 4, 101, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1015, '角色导出', 'system:role:export', 3, 5, 101, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1016, '分配菜单权限', 'system:permission:assign-role-menu', 3, 6, 101, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),

-- 菜单管理按钮
(1021, '菜单查询', 'system:menu:query', 3, 1, 102, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1022, '菜单新增', 'system:menu:create', 3, 2, 102, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1023, '菜单修改', 'system:menu:update', 3, 3, 102, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1024, '菜单删除', 'system:menu:delete', 3, 4, 102, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),

-- 部门管理按钮
(1031, '部门查询', 'system:dept:query', 3, 1, 103, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1032, '部门新增', 'system:dept:create', 3, 2, 103, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1033, '部门修改', 'system:dept:update', 3, 3, 103, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1034, '部门删除', 'system:dept:delete', 3, 4, 103, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),

-- 岗位管理按钮
(1041, '岗位查询', 'system:post:query', 3, 1, 104, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1042, '岗位新增', 'system:post:create', 3, 2, 104, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1043, '岗位修改', 'system:post:update', 3, 3, 104, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1044, '岗位删除', 'system:post:delete', 3, 4, 104, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1045, '岗位导出', 'system:post:export', 3, 5, 104, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),

-- 字典管理按钮
(1051, '字典查询', 'system:dict:query', 3, 1, 105, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1052, '字典新增', 'system:dict:create', 3, 2, 105, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1053, '字典修改', 'system:dict:update', 3, 3, 105, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1054, '字典删除', 'system:dict:delete', 3, 4, 105, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1055, '字典导出', 'system:dict:export', 3, 5, 105, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),

-- 通知公告按钮
(1061, '公告查询', 'system:notice:query', 3, 1, 106, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1062, '公告新增', 'system:notice:create', 3, 2, 106, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1063, '公告修改', 'system:notice:update', 3, 3, 106, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1064, '公告删除', 'system:notice:delete', 3, 4, 106, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),

-- 操作日志按钮
(1071, '日志查询', 'system:operate-log:query', 3, 1, 107, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1072, '日志导出', 'system:operate-log:export', 3, 2, 107, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),

-- 登录日志按钮
(1081, '日志查询', 'system:login-log:query', 3, 1, 108, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1082, '日志导出', 'system:login-log:export', 3, 2, 108, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),

-- 租户管理按钮
(1091, '租户查询', 'system:tenant:query', 3, 1, 109, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1092, '租户新增', 'system:tenant:create', 3, 2, 109, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1093, '租户修改', 'system:tenant:update', 3, 3, 109, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1094, '租户删除', 'system:tenant:delete', 3, 4, 109, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(1095, '租户导出', 'system:tenant:export', 3, 5, 109, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),

-- 参数配置按钮
(2001, '配置查询', 'infra:config:query', 3, 1, 200, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(2002, '配置新增', 'infra:config:create', 3, 2, 200, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(2003, '配置修改', 'infra:config:update', 3, 3, 200, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(2004, '配置删除', 'infra:config:delete', 3, 4, 200, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(2005, '配置导出', 'infra:config:export', 3, 5, 200, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),

-- 文件管理按钮
(2011, '文件查询', 'infra:file:query', 3, 1, 201, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(2012, '文件上传', 'infra:file:create', 3, 2, 201, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(2013, '文件删除', 'infra:file:delete', 3, 3, 201, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system');

-- ---------------------------- 字典 ----------------------------
INSERT INTO sys_dict_type (id, name, type, status, remark, creator, updater)
VALUES (1, '系统状态', 'common_status', 0, '通用的启用 / 禁用状态', 'system', 'system'),
       (2, '用户性别', 'system_user_sex', 0, NULL, 'system', 'system'),
       (3, '菜单类型', 'system_menu_type', 0, NULL, 'system', 'system'),
       (4, '角色类型', 'system_role_type', 0, NULL, 'system', 'system'),
       (5, '公告类型', 'system_notice_type', 0, NULL, 'system', 'system'),
       (6, '登录日志类型', 'system_login_type', 0, NULL, 'system', 'system'),
       (7, '登录结果', 'system_login_result', 0, NULL, 'system', 'system'),
       (8, '布尔字符串', 'infra_boolean_string', 0, '值为 true / false 的字符串', 'system', 'system'),
       (9, '参数类型', 'infra_config_type', 0, NULL, 'system', 'system');

INSERT INTO sys_dict_data (id, sort, label, `value`, dict_type, status, color_type, css_class, creator, updater)
VALUES (1, 1, '开启', '0', 'common_status', 0, 'success', '', 'system', 'system'),
       (2, 2, '关闭', '1', 'common_status', 0, 'danger', '', 'system', 'system'),

       (11, 1, '男', '1', 'system_user_sex', 0, '', '', 'system', 'system'),
       (12, 2, '女', '2', 'system_user_sex', 0, '', '', 'system', 'system'),

       (21, 1, '目录', '1', 'system_menu_type', 0, '', '', 'system', 'system'),
       (22, 2, '菜单', '2', 'system_menu_type', 0, '', '', 'system', 'system'),
       (23, 3, '按钮', '3', 'system_menu_type', 0, '', '', 'system', 'system'),

       (31, 1, '内置角色', '1', 'system_role_type', 0, 'danger', '', 'system', 'system'),
       (32, 2, '自定义角色', '2', 'system_role_type', 0, 'primary', '', 'system', 'system'),

       (41, 1, '通知', '1', 'system_notice_type', 0, 'success', '', 'system', 'system'),
       (42, 2, '公告', '2', 'system_notice_type', 0, 'warning', '', 'system', 'system'),

       (51, 1, '登录', '1', 'system_login_type', 0, 'primary', '', 'system', 'system'),
       (52, 2, '登出', '2', 'system_login_type', 0, 'info', '', 'system', 'system'),

       (61, 1, '成功', '0', 'system_login_result', 0, 'success', '', 'system', 'system'),
       (62, 2, '账号或密码不正确', '1', 'system_login_result', 0, 'danger', '', 'system', 'system'),
       (63, 3, '账号被停用', '2', 'system_login_result', 0, 'danger', '', 'system', 'system'),

       (71, 1, '是', 'true', 'infra_boolean_string', 0, 'success', '', 'system', 'system'),
       (72, 2, '否', 'false', 'infra_boolean_string', 0, 'danger', '', 'system', 'system'),

       (81, 1, '系统内置', '1', 'infra_config_type', 0, 'danger', '', 'system', 'system'),
       (82, 2, '自定义', '2', 'infra_config_type', 0, 'primary', '', 'system', 'system');

-- ---------------------------- 参数配置 ----------------------------
INSERT INTO sys_config (id, category, name, config_key, config_value, type, visible, remark, creator, updater)
VALUES (1, 'user', '用户管理-账号初始密码', 'sys.user.init-password', '123456', 1, 0, '新增用户时的初始密码',
        'system', 'system'),
       (2, 'ui', '控制台-页脚展示文案', 'ui.footer.text', '校园二手书管理', 2, 1, '前端可读，用于页脚展示',
        'system', 'system');

-- ---------------------------- 通知公告 ----------------------------
INSERT INTO sys_notice (id, tenant_id, title, content, type, status, creator, updater)
VALUES (1, 1, '欢迎使用校园二手书平台', '<p>请文明交易、当面验书；平台不经手资金，仅提供预约与留言。</p>', 1, 0,
        'system', 'system');
