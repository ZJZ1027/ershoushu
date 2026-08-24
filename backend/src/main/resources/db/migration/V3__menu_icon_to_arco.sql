-- ----------------------------------------------------------------------------
-- 菜单图标改用 Arco 图标名（`arco:` 前缀 + Arco 组件名）。
--
-- 背景：原先存的是 Iconify 名（ep:*）。前端图标选择器给的是 Element Plus / Font Awesome
-- 三整套上千个图标，而侧栏为统一视觉风格另有一张手写的 Iconify→Arco 映射表，两个集合对不上，
-- 选到映射表外的图标时侧栏静默退回默认图标 —— 菜单管理里配的图标和实际显示的不是一个。
-- 现在选择器和所有渲染点共用同一张 Arco 注册表，存库值即渲染组件名，不再有中间映射。
-- 顺带解决离线问题：Iconify 名是运行时才知道的，打包扫不到、要联网向 api.iconify.design 取图。
--
-- 按 icon 旧值匹配而不是按 id：只改仍是种子默认值的行，不覆盖使用者在界面上改过的图标；
-- 重复执行也不会有副作用。V2 保持原样不动，避免破坏已应用迁移的 checksum 校验。
-- ----------------------------------------------------------------------------

UPDATE sys_menu SET icon = 'arco:IconSettings' WHERE icon = 'ep:tools'; -- 系统管理
UPDATE sys_menu SET icon = 'arco:IconDesktop' WHERE icon = 'ep:monitor'; -- 基础设施
UPDATE sys_menu SET icon = 'arco:IconHistory' WHERE icon = 'ep:document'; -- 审计日志
UPDATE sys_menu SET icon = 'arco:IconUser' WHERE icon = 'ep:avatar'; -- 用户管理
UPDATE sys_menu SET icon = 'arco:IconUserGroup' WHERE icon = 'ep:coordinate'; -- 角色管理
UPDATE sys_menu SET icon = 'arco:IconMenu' WHERE icon = 'ep:menu'; -- 菜单管理
UPDATE sys_menu SET icon = 'arco:IconMindMapping' WHERE icon = 'ep:office-building'; -- 部门管理
UPDATE sys_menu SET icon = 'arco:IconIdcard' WHERE icon = 'ep:briefcase'; -- 岗位管理
UPDATE sys_menu SET icon = 'arco:IconBook' WHERE icon = 'ep:collection'; -- 字典管理
UPDATE sys_menu SET icon = 'arco:IconNotification' WHERE icon = 'ep:chat-line-square'; -- 通知公告
UPDATE sys_menu SET icon = 'arco:IconStamp' WHERE icon = 'ep:document-copy'; -- 操作日志
UPDATE sys_menu SET icon = 'arco:IconSafe' WHERE icon = 'ep:key'; -- 登录日志
UPDATE sys_menu SET icon = 'arco:IconHome' WHERE icon = 'ep:house'; -- 租户管理
UPDATE sys_menu SET icon = 'arco:IconTool' WHERE icon = 'ep:setting'; -- 参数配置
UPDATE sys_menu SET icon = 'arco:IconFolder' WHERE icon = 'ep:files'; -- 文件管理
