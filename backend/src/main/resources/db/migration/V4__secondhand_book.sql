-- 校园二手书：用户资料字段、业务表、字典、菜单、演示账号。
-- 菜单 id 段位：目录 4、业务菜单 300~399、按钮 3000+。

ALTER TABLE sys_user
    ADD COLUMN wechat varchar(64) DEFAULT NULL COMMENT '微信号' AFTER avatar,
    ADD COLUMN campus varchar(64) DEFAULT NULL COMMENT '校区' AFTER wechat;

-- ---------------------------- 分类 ----------------------------
CREATE TABLE bu_category
(
    id          bigint       NOT NULL AUTO_INCREMENT COMMENT '分类编号',
    tenant_id   bigint       NOT NULL DEFAULT 1 COMMENT '租户编号',
    name        varchar(64)  NOT NULL COMMENT '分类名称',
    icon        varchar(128)          DEFAULT NULL COMMENT '图标',
    sort        int          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    status      tinyint      NOT NULL DEFAULT 0 COMMENT '状态：0 正常 1 停用',
    remark      varchar(500)          DEFAULT NULL COMMENT '备注',
    creator     varchar(64)           DEFAULT NULL,
    create_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)           DEFAULT NULL,
    update_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     tinyint      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_bu_category_sort (tenant_id, sort)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '书籍分类';

-- ---------------------------- 书籍 ----------------------------
CREATE TABLE bu_book
(
    id            bigint         NOT NULL AUTO_INCREMENT COMMENT '书籍编号',
    tenant_id     bigint         NOT NULL DEFAULT 1,
    seller_id     bigint         NOT NULL COMMENT '卖家用户编号',
    category_id   bigint                  DEFAULT NULL COMMENT '分类编号',
    title         varchar(128)   NOT NULL COMMENT '书名',
    author        varchar(128)            DEFAULT NULL COMMENT '作者',
    isbn          varchar(32)             DEFAULT NULL COMMENT 'ISBN',
    publisher     varchar(128)            DEFAULT NULL COMMENT '出版社',
    course_name   varchar(128)            DEFAULT NULL COMMENT '课程名',
    major_name    varchar(128)            DEFAULT NULL COMMENT '专业',
    condition_code varchar(32)   NOT NULL DEFAULT 'used' COMMENT '成色字典 book_condition',
    origin_price  decimal(10, 2)          DEFAULT NULL COMMENT '原价',
    price         decimal(10, 2) NOT NULL COMMENT '售价',
    campus        varchar(64)             DEFAULT NULL COMMENT '校区',
    meetup_place  varchar(256)            DEFAULT NULL COMMENT '期望面交地点',
    description   varchar(2000)           DEFAULT NULL COMMENT '描述',
    cover_url     varchar(512)            DEFAULT NULL COMMENT '封面',
    status        tinyint        NOT NULL DEFAULT 0 COMMENT '0待审 1在售 2预约中 3已成交 4已下架 5驳回',
    reject_reason varchar(500)            DEFAULT NULL COMMENT '驳回原因',
    creator       varchar(64)             DEFAULT NULL,
    create_time   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater       varchar(64)             DEFAULT NULL,
    update_time   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       tinyint        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_bu_book_seller (seller_id, tenant_id),
    KEY idx_bu_book_status (status, tenant_id),
    KEY idx_bu_book_category (category_id, tenant_id),
    KEY idx_bu_book_title (title)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '二手书';

CREATE TABLE bu_book_image
(
    id          bigint       NOT NULL AUTO_INCREMENT,
    tenant_id   bigint       NOT NULL DEFAULT 1,
    book_id     bigint       NOT NULL COMMENT '书籍编号',
    url         varchar(512) NOT NULL COMMENT '图片地址',
    sort        int          NOT NULL DEFAULT 0,
    creator     varchar(64)           DEFAULT NULL,
    create_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)           DEFAULT NULL,
    update_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     tinyint      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_bu_book_image_book (book_id, tenant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '书籍图片';

CREATE TABLE bu_favorite
(
    id          bigint   NOT NULL AUTO_INCREMENT,
    tenant_id   bigint   NOT NULL DEFAULT 1,
    user_id     bigint   NOT NULL,
    book_id     bigint   NOT NULL,
    creator     varchar(64)       DEFAULT NULL,
    create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)       DEFAULT NULL,
    update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     tinyint  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_bu_favorite_user (user_id, tenant_id),
    KEY idx_bu_favorite_book (book_id, user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '收藏';

CREATE TABLE bu_inquiry
(
    id          bigint   NOT NULL AUTO_INCREMENT,
    tenant_id   bigint   NOT NULL DEFAULT 1,
    book_id     bigint   NOT NULL,
    buyer_id    bigint   NOT NULL,
    seller_id   bigint   NOT NULL,
    last_msg    varchar(500)      DEFAULT NULL,
    last_time   datetime          DEFAULT NULL,
    creator     varchar(64)       DEFAULT NULL,
    create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)       DEFAULT NULL,
    update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     tinyint  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_bu_inquiry_book_buyer (book_id, buyer_id, tenant_id),
    KEY idx_bu_inquiry_seller (seller_id, tenant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '留言会话';

CREATE TABLE bu_inquiry_msg
(
    id          bigint        NOT NULL AUTO_INCREMENT,
    tenant_id   bigint        NOT NULL DEFAULT 1,
    inquiry_id  bigint        NOT NULL,
    sender_id   bigint        NOT NULL,
    content     varchar(1000) NOT NULL,
    creator     varchar(64)            DEFAULT NULL,
    create_time datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)            DEFAULT NULL,
    update_time datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     tinyint       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_bu_inquiry_msg (inquiry_id, tenant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '留言内容';

CREATE TABLE bu_order
(
    id                bigint       NOT NULL AUTO_INCREMENT,
    tenant_id         bigint       NOT NULL DEFAULT 1,
    order_no          varchar(32)  NOT NULL COMMENT '预约单号',
    book_id           bigint       NOT NULL,
    buyer_id          bigint       NOT NULL,
    seller_id         bigint       NOT NULL,
    status            tinyint      NOT NULL DEFAULT 0 COMMENT '0待确认 1已同意 2已完成 3已取消',
    meetup_time       datetime              DEFAULT NULL,
    meetup_place      varchar(256)          DEFAULT NULL,
    remark            varchar(500)          DEFAULT NULL COMMENT '买家留言',
    cancel_reason     varchar(500)          DEFAULT NULL,
    buyer_confirmed   tinyint      NOT NULL DEFAULT 0,
    seller_confirmed  tinyint      NOT NULL DEFAULT 0,
    creator           varchar(64)           DEFAULT NULL,
    create_time       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater           varchar(64)           DEFAULT NULL,
    update_time       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted           tinyint      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_bu_order_no (order_no, tenant_id),
    KEY idx_bu_order_book (book_id, tenant_id),
    KEY idx_bu_order_buyer (buyer_id, tenant_id),
    KEY idx_bu_order_seller (seller_id, tenant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '线下预约单';

CREATE TABLE bu_report
(
    id            bigint       NOT NULL AUTO_INCREMENT,
    tenant_id     bigint       NOT NULL DEFAULT 1,
    reporter_id   bigint       NOT NULL,
    target_type   tinyint      NOT NULL COMMENT '1书籍 2用户',
    target_id     bigint       NOT NULL,
    reason_code   varchar(32)           DEFAULT NULL COMMENT '字典 report_reason',
    content       varchar(1000)         DEFAULT NULL,
    status        tinyint      NOT NULL DEFAULT 0 COMMENT '0待处理 1已处理 2驳回',
    handle_remark varchar(500)          DEFAULT NULL,
    creator       varchar(64)           DEFAULT NULL,
    create_time   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater       varchar(64)           DEFAULT NULL,
    update_time   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       tinyint      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_bu_report_status (status, tenant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '举报';

-- 用户端角色
INSERT INTO sys_role (id, tenant_id, name, code, sort, status, type, remark, creator, updater)
VALUES (3, 1, '校园用户', 'app_user', 3, 0, 2, '用户端注册默认角色，无后台菜单', 'system', 'system');

-- 演示账号密码均为 admin123
INSERT INTO sys_user (id, tenant_id, username, password, nickname, remark, dept_id, email, mobile, sex, status,
                      wechat, campus, creator, updater)
VALUES (2, 1, 'student1', '$2a$10$uFfmgGay7TKGr.2w2GdA7efj/w7BvV/tsRZ.i3N6x/taCMPHwz8lC', '书虫小王', '演示卖家',
        NULL, NULL, '13900000001', 1, 0, 'wang-book', '本部', 'system', 'system'),
       (3, 1, 'student2', '$2a$10$uFfmgGay7TKGr.2w2GdA7efj/w7BvV/tsRZ.i3N6x/taCMPHwz8lC', '考研小李', '演示买家',
        NULL, NULL, '13900000002', 2, 0, 'li-kaoyan', '本部', 'system', 'system');

INSERT INTO sys_user_role (id, tenant_id, user_id, role_id, creator, updater)
VALUES (2, 1, 2, 3, 'system', 'system'),
       (3, 1, 3, 3, 'system', 'system');

INSERT INTO bu_category (id, tenant_id, name, icon, sort, status, creator, updater)
VALUES (1, 1, '教材教辅', 'book', 1, 0, 'system', 'system'),
       (2, 1, '考研资料', 'trophy', 2, 0, 'system', 'system'),
       (3, 1, '课外读物', 'star', 3, 0, 'system', 'system'),
       (4, 1, '工具书', 'tool', 4, 0, 'system', 'system'),
       (5, 1, '资格考试', 'idcard', 5, 0, 'system', 'system');

INSERT INTO sys_dict_type (id, name, type, status, remark, creator, updater)
VALUES (10, '书籍成色', 'book_condition', 0, '二手书成色', 'system', 'system'),
       (11, '校区', 'campus', 0, '面交校区', 'system', 'system'),
       (12, '举报原因', 'report_reason', 0, NULL, 'system', 'system'),
       (13, '书籍状态', 'book_status', 0, NULL, 'system', 'system'),
       (14, '预约状态', 'order_status', 0, NULL, 'system', 'system');

INSERT INTO sys_dict_data (id, sort, label, `value`, dict_type, status, color_type, css_class, creator, updater)
VALUES (100, 1, '全新', 'new', 'book_condition', 0, 'success', '', 'system', 'system'),
       (101, 2, '几乎全新', 'like_new', 'book_condition', 0, 'primary', '', 'system', 'system'),
       (102, 3, '轻微痕迹', 'good', 'book_condition', 0, 'warning', '', 'system', 'system'),
       (103, 4, '明显使用', 'used', 'book_condition', 0, 'info', '', 'system', 'system'),
       (110, 1, '本部', '本部', 'campus', 0, '', '', 'system', 'system'),
       (111, 2, '东校区', '东校区', 'campus', 0, '', '', 'system', 'system'),
       (112, 3, '西校区', '西校区', 'campus', 0, '', '', 'system', 'system'),
       (120, 1, '虚假信息', 'fake', 'report_reason', 0, 'danger', '', 'system', 'system'),
       (121, 2, '违规内容', 'illegal', 'report_reason', 0, 'danger', '', 'system', 'system'),
       (122, 3, '已售仍挂', 'sold', 'report_reason', 0, 'warning', '', 'system', 'system'),
       (123, 4, '其他', 'other', 'report_reason', 0, 'info', '', 'system', 'system'),
       (130, 1, '待审', '0', 'book_status', 0, 'warning', '', 'system', 'system'),
       (131, 2, '在售', '1', 'book_status', 0, 'success', '', 'system', 'system'),
       (132, 3, '预约中', '2', 'book_status', 0, 'primary', '', 'system', 'system'),
       (133, 4, '已成交', '3', 'book_status', 0, 'info', '', 'system', 'system'),
       (134, 5, '已下架', '4', 'book_status', 0, '', '', 'system', 'system'),
       (135, 6, '已驳回', '5', 'book_status', 0, 'danger', '', 'system', 'system'),
       (140, 1, '待确认', '0', 'order_status', 0, 'warning', '', 'system', 'system'),
       (141, 2, '已同意', '1', 'order_status', 0, 'primary', '', 'system', 'system'),
       (142, 3, '已完成', '2', 'order_status', 0, 'success', '', 'system', 'system'),
       (143, 4, '已取消', '3', 'order_status', 0, 'info', '', 'system', 'system');

INSERT INTO sys_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                      status, visible, keep_alive, always_show, creator, updater)
VALUES
(4, '二手书管理', '', 1, 5, 0, '/business', 'arco:IconBook', NULL, NULL, 0, 1, 1, 1, 'system', 'system'),
(300, '工作台', '', 2, 1, 4, 'dashboard', 'arco:IconDashboard', 'business/dashboard/index', 'BusinessDashboard', 0, 1, 1, 1, 'system', 'system'),
(301, '分类管理', '', 2, 2, 4, 'category', 'arco:IconApps', 'business/category/index', 'BusinessCategory', 0, 1, 1, 1, 'system', 'system'),
(302, '书籍审核', '', 2, 3, 4, 'book', 'arco:IconBook', 'business/book/index', 'BusinessBook', 0, 1, 1, 1, 'system', 'system'),
(303, '预约单', '', 2, 4, 4, 'order', 'arco:IconCalendar', 'business/order/index', 'BusinessOrder', 0, 1, 1, 1, 'system', 'system'),
(304, '留言抽查', '', 2, 5, 4, 'inquiry', 'arco:IconMessage', 'business/inquiry/index', 'BusinessInquiry', 0, 1, 1, 1, 'system', 'system'),
(305, '会员管理', '', 2, 6, 4, 'member', 'arco:IconUserGroup', 'business/member/index', 'BusinessMember', 0, 1, 1, 1, 'system', 'system'),
(306, '举报处理', '', 2, 7, 4, 'report', 'arco:IconExclamationCircle', 'business/report/index', 'BusinessReport', 0, 1, 1, 1, 'system', 'system'),
(3001, '工作台查询', 'business:dashboard:query', 3, 1, 300, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(3011, '分类查询', 'business:category:query', 3, 1, 301, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(3012, '分类新增', 'business:category:create', 3, 2, 301, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(3013, '分类修改', 'business:category:update', 3, 3, 301, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(3014, '分类删除', 'business:category:delete', 3, 4, 301, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(3021, '书籍查询', 'business:book:query', 3, 1, 302, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(3022, '书籍审核', 'business:book:audit', 3, 2, 302, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(3023, '书籍下架', 'business:book:offshelf', 3, 3, 302, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(3031, '预约查询', 'business:order:query', 3, 1, 303, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(3032, '预约关闭', 'business:order:close', 3, 2, 303, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(3041, '留言查询', 'business:inquiry:query', 3, 1, 304, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(3051, '会员查询', 'business:member:query', 3, 1, 305, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(3052, '会员停用', 'business:member:update', 3, 2, 305, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(3061, '举报查询', 'business:report:query', 3, 1, 306, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system'),
(3062, '举报处理', 'business:report:handle', 3, 2, 306, '', '', '', NULL, 0, 1, 1, 1, 'system', 'system');

INSERT INTO bu_book (id, tenant_id, seller_id, category_id, title, author, isbn, publisher, course_name, major_name,
                     condition_code, origin_price, price, campus, meetup_place, description, status, creator, updater)
VALUES (1, 1, 2, 1, '高等数学（上册）', '同济大学数学系', '9787040396638', '高等教育出版社', '高等数学', '工科',
        'good', 56.00, 18.00, '本部', '一食堂门口', '有少量笔记，无缺页。', 1, 'student1', 'student1'),
       (2, 1, 2, 2, '张宇1000题（数学一）', '张宇', NULL, '北京理工大学出版社', NULL, '考研',
        'like_new', 68.00, 35.00, '本部', '图书馆大厅', '几乎全新，做过一小部分。', 1, 'student1', 'student1'),
       (3, 1, 2, 3, '三体', '刘慈欣', '9787536692930', '重庆出版社', NULL, NULL,
        'used', 23.00, 8.00, '东校区', '东门快递点', '封面有折痕，内容完好。', 0, 'student1', 'student1');
