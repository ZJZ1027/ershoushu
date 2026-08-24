-- ----------------------------------------------------------------------------
-- 基础表结构。约定：
--   1. 主键统一 bigint 自增；
--   2. 审计字段 creator / create_time / updater / update_time / deleted 由代码自动填充；
--   3. deleted = 1 表示逻辑删除，因此业务唯一性（如用户名）由代码校验，不建唯一索引；
--   4. 带 tenant_id 的表由 MyBatis-Plus 多租户插件自动追加过滤条件。
-- ----------------------------------------------------------------------------

-- ---------------------------- 租户 ----------------------------
CREATE TABLE sys_tenant
(
    id             bigint       NOT NULL AUTO_INCREMENT COMMENT '租户编号',
    name           varchar(64)  NOT NULL COMMENT '租户名',
    contact_name   varchar(64)           DEFAULT NULL COMMENT '联系人',
    contact_mobile varchar(32)           DEFAULT NULL COMMENT '联系手机',
    status         tinyint      NOT NULL DEFAULT 0 COMMENT '状态：0 正常 1 停用',
    domain         varchar(256)          DEFAULT NULL COMMENT '绑定域名',
    expire_time    datetime              DEFAULT NULL COMMENT '过期时间',
    account_count  int          NOT NULL DEFAULT 0 COMMENT '账号数量上限',
    creator        varchar(64)           DEFAULT NULL COMMENT '创建人',
    create_time    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater        varchar(64)           DEFAULT NULL COMMENT '更新人',
    update_time    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        tinyint      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '租户';

-- ---------------------------- 用户 / 组织 ----------------------------
CREATE TABLE sys_user
(
    id          bigint       NOT NULL AUTO_INCREMENT COMMENT '用户编号',
    tenant_id   bigint       NOT NULL DEFAULT 1 COMMENT '租户编号',
    username    varchar(64)  NOT NULL COMMENT '用户账号',
    password    varchar(128) NOT NULL DEFAULT '' COMMENT '密码（BCrypt）',
    nickname    varchar(64)  NOT NULL COMMENT '用户昵称',
    remark      varchar(500)          DEFAULT NULL COMMENT '备注',
    dept_id     bigint                DEFAULT NULL COMMENT '部门编号',
    email       varchar(64)           DEFAULT NULL COMMENT '邮箱',
    mobile      varchar(32)           DEFAULT NULL COMMENT '手机号',
    sex         tinyint               DEFAULT NULL COMMENT '性别：1 男 2 女',
    avatar      varchar(512)          DEFAULT NULL COMMENT '头像地址',
    status      tinyint      NOT NULL DEFAULT 0 COMMENT '状态：0 正常 1 停用',
    login_ip    varchar(64)           DEFAULT NULL COMMENT '最后登录 IP',
    login_date  datetime              DEFAULT NULL COMMENT '最后登录时间',
    creator     varchar(64)           DEFAULT NULL COMMENT '创建人',
    create_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)           DEFAULT NULL COMMENT '更新人',
    update_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_sys_user_username (username, tenant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '用户';

CREATE TABLE sys_dept
(
    id             bigint      NOT NULL AUTO_INCREMENT COMMENT '部门编号',
    tenant_id      bigint      NOT NULL DEFAULT 1 COMMENT '租户编号',
    name           varchar(64) NOT NULL COMMENT '部门名称',
    parent_id      bigint      NOT NULL DEFAULT 0 COMMENT '父部门编号',
    sort           int         NOT NULL DEFAULT 0 COMMENT '显示顺序',
    leader_user_id bigint               DEFAULT NULL COMMENT '负责人用户编号',
    phone          varchar(32)          DEFAULT NULL COMMENT '联系电话',
    email          varchar(64)          DEFAULT NULL COMMENT '邮箱',
    status         tinyint     NOT NULL DEFAULT 0 COMMENT '状态：0 正常 1 停用',
    creator        varchar(64)          DEFAULT NULL COMMENT '创建人',
    create_time    datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater        varchar(64)          DEFAULT NULL COMMENT '更新人',
    update_time    datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        tinyint     NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_sys_dept_parent (parent_id, tenant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '部门';

CREATE TABLE sys_post
(
    id          bigint      NOT NULL AUTO_INCREMENT COMMENT '岗位编号',
    tenant_id   bigint      NOT NULL DEFAULT 1 COMMENT '租户编号',
    code        varchar(64) NOT NULL COMMENT '岗位编码',
    name        varchar(64) NOT NULL COMMENT '岗位名称',
    sort        int         NOT NULL DEFAULT 0 COMMENT '显示顺序',
    status      tinyint     NOT NULL DEFAULT 0 COMMENT '状态：0 正常 1 停用',
    remark      varchar(500)         DEFAULT NULL COMMENT '备注',
    creator     varchar(64)          DEFAULT NULL COMMENT '创建人',
    create_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)          DEFAULT NULL COMMENT '更新人',
    update_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint     NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '岗位';

CREATE TABLE sys_user_post
(
    id          bigint     NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id   bigint     NOT NULL DEFAULT 1 COMMENT '租户编号',
    user_id     bigint     NOT NULL COMMENT '用户编号',
    post_id     bigint     NOT NULL COMMENT '岗位编号',
    creator     varchar(64)         DEFAULT NULL COMMENT '创建人',
    create_time datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)         DEFAULT NULL COMMENT '更新人',
    update_time datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint    NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_sys_user_post_user (user_id, tenant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '用户岗位关联';

-- ---------------------------- 角色 / 菜单 ----------------------------
CREATE TABLE sys_role
(
    id          bigint      NOT NULL AUTO_INCREMENT COMMENT '角色编号',
    tenant_id   bigint      NOT NULL DEFAULT 1 COMMENT '租户编号',
    name        varchar(64) NOT NULL COMMENT '角色名称',
    code        varchar(64) NOT NULL COMMENT '角色标识',
    sort        int         NOT NULL DEFAULT 0 COMMENT '显示顺序',
    status      tinyint     NOT NULL DEFAULT 0 COMMENT '状态：0 正常 1 停用',
    type        tinyint     NOT NULL DEFAULT 2 COMMENT '角色类型：1 内置 2 自定义',
    remark      varchar(500)         DEFAULT NULL COMMENT '备注',
    creator     varchar(64)          DEFAULT NULL COMMENT '创建人',
    create_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)          DEFAULT NULL COMMENT '更新人',
    update_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint     NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '角色';

CREATE TABLE sys_user_role
(
    id          bigint     NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id   bigint     NOT NULL DEFAULT 1 COMMENT '租户编号',
    user_id     bigint     NOT NULL COMMENT '用户编号',
    role_id     bigint     NOT NULL COMMENT '角色编号',
    creator     varchar(64)         DEFAULT NULL COMMENT '创建人',
    create_time datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)         DEFAULT NULL COMMENT '更新人',
    update_time datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint    NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_sys_user_role_user (user_id, tenant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '用户角色关联';

CREATE TABLE sys_menu
(
    id             bigint      NOT NULL AUTO_INCREMENT COMMENT '菜单编号',
    name           varchar(64) NOT NULL COMMENT '菜单名称',
    permission     varchar(128)         DEFAULT '' COMMENT '权限标识',
    type           tinyint     NOT NULL COMMENT '菜单类型：1 目录 2 菜单 3 按钮',
    sort           int         NOT NULL DEFAULT 0 COMMENT '显示顺序',
    parent_id      bigint      NOT NULL DEFAULT 0 COMMENT '父菜单编号',
    path           varchar(256)         DEFAULT NULL COMMENT '路由地址',
    icon           varchar(128)         DEFAULT NULL COMMENT '菜单图标',
    component      varchar(256)         DEFAULT NULL COMMENT '组件路径',
    component_name varchar(256)         DEFAULT NULL COMMENT '组件名',
    status         tinyint     NOT NULL DEFAULT 0 COMMENT '状态：0 正常 1 停用',
    visible        tinyint     NOT NULL DEFAULT 1 COMMENT '是否可见',
    keep_alive     tinyint     NOT NULL DEFAULT 1 COMMENT '是否缓存',
    always_show    tinyint     NOT NULL DEFAULT 1 COMMENT '是否总是显示',
    creator        varchar(64)          DEFAULT NULL COMMENT '创建人',
    create_time    datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater        varchar(64)          DEFAULT NULL COMMENT '更新人',
    update_time    datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        tinyint     NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_sys_menu_parent (parent_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '菜单权限';

CREATE TABLE sys_role_menu
(
    id          bigint     NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id   bigint     NOT NULL DEFAULT 1 COMMENT '租户编号',
    role_id     bigint     NOT NULL COMMENT '角色编号',
    menu_id     bigint     NOT NULL COMMENT '菜单编号',
    creator     varchar(64)         DEFAULT NULL COMMENT '创建人',
    create_time datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)         DEFAULT NULL COMMENT '更新人',
    update_time datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint    NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_sys_role_menu_role (role_id, tenant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '角色菜单关联';

-- ---------------------------- 字典 / 参数 / 公告 ----------------------------
CREATE TABLE sys_dict_type
(
    id          bigint       NOT NULL AUTO_INCREMENT COMMENT '字典编号',
    name        varchar(100) NOT NULL COMMENT '字典名称',
    type        varchar(100) NOT NULL COMMENT '字典类型',
    status      tinyint      NOT NULL DEFAULT 0 COMMENT '状态：0 正常 1 停用',
    remark      varchar(500)          DEFAULT NULL COMMENT '备注',
    creator     varchar(64)           DEFAULT NULL COMMENT '创建人',
    create_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)           DEFAULT NULL COMMENT '更新人',
    update_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_sys_dict_type_type (type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '字典类型';

CREATE TABLE sys_dict_data
(
    id          bigint       NOT NULL AUTO_INCREMENT COMMENT '字典数据编号',
    sort        int          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    label       varchar(100) NOT NULL COMMENT '字典标签',
    `value`     varchar(100) NOT NULL COMMENT '字典键值',
    dict_type   varchar(100) NOT NULL COMMENT '字典类型',
    status      tinyint      NOT NULL DEFAULT 0 COMMENT '状态：0 正常 1 停用',
    color_type  varchar(100)          DEFAULT '' COMMENT '颜色类型',
    css_class   varchar(100)          DEFAULT '' COMMENT 'CSS 样式',
    remark      varchar(500)          DEFAULT NULL COMMENT '备注',
    creator     varchar(64)           DEFAULT NULL COMMENT '创建人',
    create_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)           DEFAULT NULL COMMENT '更新人',
    update_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_sys_dict_data_type (dict_type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '字典数据';

CREATE TABLE sys_config
(
    id           bigint       NOT NULL AUTO_INCREMENT COMMENT '参数编号',
    category     varchar(64)  NOT NULL COMMENT '参数分组',
    name         varchar(100) NOT NULL COMMENT '参数名称',
    config_key   varchar(128) NOT NULL COMMENT '参数键名',
    config_value varchar(500) NOT NULL COMMENT '参数键值',
    type         tinyint      NOT NULL DEFAULT 2 COMMENT '参数类型：1 系统内置 2 自定义',
    visible      tinyint      NOT NULL DEFAULT 1 COMMENT '是否可见（前端可读）',
    remark       varchar(500)          DEFAULT NULL COMMENT '备注',
    creator      varchar(64)           DEFAULT NULL COMMENT '创建人',
    create_time  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater      varchar(64)           DEFAULT NULL COMMENT '更新人',
    update_time  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      tinyint      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_sys_config_key (config_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '参数配置';

CREATE TABLE sys_notice
(
    id          bigint       NOT NULL AUTO_INCREMENT COMMENT '公告编号',
    tenant_id   bigint       NOT NULL DEFAULT 1 COMMENT '租户编号',
    title       varchar(128) NOT NULL COMMENT '公告标题',
    content     text COMMENT '公告内容',
    type        tinyint      NOT NULL DEFAULT 1 COMMENT '公告类型：1 通知 2 公告',
    status      tinyint      NOT NULL DEFAULT 0 COMMENT '状态：0 正常 1 关闭',
    remark      varchar(500)          DEFAULT NULL COMMENT '备注',
    creator     varchar(64)           DEFAULT NULL COMMENT '创建人',
    create_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)           DEFAULT NULL COMMENT '更新人',
    update_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '通知公告';

-- ---------------------------- 日志 / 文件 / 令牌 ----------------------------
CREATE TABLE sys_login_log
(
    id          bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id   bigint      NOT NULL DEFAULT 1 COMMENT '租户编号',
    log_type    tinyint     NOT NULL COMMENT '日志类型：1 登录 2 登出',
    user_id     bigint               DEFAULT NULL COMMENT '用户编号',
    username    varchar(64) NOT NULL COMMENT '用户账号',
    result      tinyint     NOT NULL DEFAULT 0 COMMENT '登录结果：0 成功 1 账号或密码不正确 2 账号被停用',
    user_ip     varchar(64)          DEFAULT NULL COMMENT '登录 IP',
    user_agent  varchar(512)         DEFAULT NULL COMMENT '浏览器 UA',
    create_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (id),
    KEY idx_sys_login_log_username (username, tenant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '登录日志';

CREATE TABLE sys_oper_log
(
    id             bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id      bigint       NOT NULL DEFAULT 1 COMMENT '租户编号',
    module         varchar(64)  NOT NULL COMMENT '模块名',
    name           varchar(128) NOT NULL COMMENT '操作名',
    user_id        bigint                DEFAULT NULL COMMENT '操作人编号',
    username       varchar(64)           DEFAULT NULL COMMENT '操作人账号',
    request_method varchar(16)           DEFAULT NULL COMMENT '请求方法',
    request_url    varchar(512)          DEFAULT NULL COMMENT '请求地址',
    request_params text COMMENT '请求参数',
    java_method    varchar(512)          DEFAULT NULL COMMENT '目标方法',
    user_ip        varchar(64)           DEFAULT NULL COMMENT '操作 IP',
    user_agent     varchar(512)          DEFAULT NULL COMMENT '浏览器 UA',
    duration       int          NOT NULL DEFAULT 0 COMMENT '执行时长（毫秒）',
    result_code    int          NOT NULL DEFAULT 0 COMMENT '结果码',
    result_msg     varchar(512)          DEFAULT NULL COMMENT '结果提示',
    create_time    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_sys_oper_log_user (user_id, tenant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '操作日志';

CREATE TABLE sys_file
(
    id          bigint        NOT NULL AUTO_INCREMENT COMMENT '文件编号',
    tenant_id   bigint        NOT NULL DEFAULT 1 COMMENT '租户编号',
    name        varchar(256)  NOT NULL COMMENT '原始文件名',
    path        varchar(512)  NOT NULL COMMENT '存储路径',
    url         varchar(1024) NOT NULL COMMENT '访问地址',
    type        varchar(128)           DEFAULT NULL COMMENT '文件类型',
    size        int           NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    creator     varchar(64)            DEFAULT NULL COMMENT '上传人',
    create_time datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    updater     varchar(64)            DEFAULT NULL COMMENT '更新人',
    update_time datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint       NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_sys_file_path (path(255))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '文件记录';

CREATE TABLE sys_token
(
    id            bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id     bigint      NOT NULL DEFAULT 1 COMMENT '租户编号',
    user_id       bigint      NOT NULL COMMENT '用户编号',
    refresh_token varchar(64) NOT NULL COMMENT '刷新令牌',
    expires_time  datetime    NOT NULL COMMENT '过期时间',
    create_time   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_token_refresh (refresh_token),
    KEY idx_sys_token_user (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '刷新令牌';
