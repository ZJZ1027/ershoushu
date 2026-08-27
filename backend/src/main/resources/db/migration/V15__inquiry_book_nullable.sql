-- 系统通知（如头像审核）可不关联具体书籍
ALTER TABLE bu_inquiry
    MODIFY COLUMN book_id bigint NULL COMMENT '书籍编号（系统通知可为空）';

-- 清理头像审核曾误把 userId 当作 bookId 写入的数据
UPDATE bu_inquiry i
    INNER JOIN sys_user p ON p.id = i.seller_id AND p.username = 'platform' AND p.deleted = 0
SET i.book_id = NULL
WHERE i.book_id = i.buyer_id
  AND i.deleted = 0;
