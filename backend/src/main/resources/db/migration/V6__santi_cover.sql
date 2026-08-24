-- 《三体》演示封面：本地文件 data/upload/2026/08/17/santi-cover.png
UPDATE bu_book
SET cover_url = '/admin-api/infra/file/view/2026/08/17/santi-cover.png',
    status    = 1,
    updater   = 'system'
WHERE id = 3
  AND title = '三体';

INSERT INTO bu_book_image (tenant_id, book_id, url, sort, creator, updater)
SELECT 1, 3, '/admin-api/infra/file/view/2026/08/17/santi-cover.png', 0, 'system', 'system'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM bu_book_image WHERE book_id = 3 AND deleted = 0 AND url LIKE '%santi-cover.png%'
);
