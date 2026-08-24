-- 草稿状态：用户端「保存」写入草稿，「提交」再进入待审
ALTER TABLE bu_book
    MODIFY COLUMN price decimal(10, 2) NULL COMMENT '售价（草稿可空）',
    MODIFY COLUMN status tinyint NOT NULL DEFAULT 0 COMMENT '0待审 1在售 2预约中 3已成交 4已下架 5驳回 6草稿';

INSERT INTO sys_dict_data (id, sort, label, `value`, dict_type, status, color_type, css_class, creator, updater)
SELECT 136, 7, '草稿', '6', 'book_status', 0, 'info', '', 'system', 'system'
FROM (SELECT 1 AS x) dummy
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE id = 136);
