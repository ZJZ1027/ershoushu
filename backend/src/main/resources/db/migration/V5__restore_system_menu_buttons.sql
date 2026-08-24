-- ----------------------------------------------------------------------------
-- 恢复系统/基础设施的按钮权限（type=3）。
--
-- 背景：这些按钮在 V2 已定义，但在开发调试中被误删，导致 super_admin 的
-- selectAllPermissions() 查不到 system:*/infra:* 权限，所有接口报 403。
-- 使用 INSERT IGNORE 幂等恢复：已存在的行跳过，重复执行无副作用。
-- ----------------------------------------------------------------------------

INSERT IGNORE INTO sys_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                             status, visible, keep_alive, always_show, creator, updater)
VALUES
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
