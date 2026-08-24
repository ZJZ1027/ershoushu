-- 新书《斯诺克的传奇》：封面 data/upload/2026/08/18/snooker-cover.png
INSERT INTO bu_book (id, tenant_id, seller_id, category_id, title, author, isbn, publisher, course_name, major_name,
                     condition_code, origin_price, price, campus, meetup_place, description, cover_url, status,
                     creator, updater)
SELECT 4, 1, 2, 3, '斯诺克的传奇', NULL, NULL, NULL, NULL, NULL,
       'like_new', NULL, 19.00, '本部', '体育馆门口', '几乎全新，适合入门了解斯诺克。',
       '/admin-api/infra/file/view/2026/08/18/snooker-cover.png', 1, 'student1', 'student1'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bu_book WHERE id = 4 AND deleted = 0);

INSERT INTO bu_book_image (tenant_id, book_id, url, sort, creator, updater)
SELECT 1, 4, '/admin-api/infra/file/view/2026/08/18/snooker-cover.png', 0, 'student1', 'student1'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM bu_book_image WHERE book_id = 4 AND deleted = 0 AND url LIKE '%snooker-cover.png%'
);
