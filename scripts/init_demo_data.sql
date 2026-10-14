-- ============================================
-- ELibrary 演示数据初始化脚本
-- 用途：为下午的演示准备完整的测试数据
-- 数据库：H2 (兼容MySQL模式)
-- ============================================

-- 清理现有数据（按依赖关系逆序删除）
DELETE FROM t_borrow_record;
DELETE FROM t_account_balance_log;
DELETE FROM t_account;
DELETE FROM t_book_access_config;
DELETE FROM t_book;
DELETE FROM t_user;

-- ============================================
-- 1. 插入演示用户
-- ============================================

-- 普通用户（免费用户）
INSERT INTO t_user (id, username, email, password_hash, user_type, user_status, balance, total_recharge, total_consume, created_at, updated_at)
VALUES (1, 'demo_user', 'demo@example.com', '123456', 'NORMAL', 'NORMAL', 100.00, 100.00, 0.00, NOW(), NOW());

-- VIP用户
INSERT INTO t_user (id, username, email, password_hash, user_type, user_status, vip_expire_at, balance, total_recharge, total_consume, created_at, updated_at)
VALUES (2, 'demo_vip', 'vip@example.com', '123456', 'VIP', 'NORMAL', DATEADD(DAY, 365, NOW()), 200.00, 200.00, 0.00, NOW(), NOW());

-- 管理员用户
INSERT INTO t_user (id, username, email, password_hash, user_type, user_status, balance, total_recharge, total_consume, created_at, updated_at)
VALUES (3, 'demo_admin', 'admin@example.com', '123456', 'VIP', 'NORMAL', 500.00, 500.00, 0.00, NOW(), NOW());

-- ============================================
-- 2. 插入用户账户
-- ============================================

INSERT INTO t_account (id, user_id, balance, total_recharge, total_consume, created_at, updated_at)
VALUES (1, 1, 100.00, 100.00, 0.00, NOW(), NOW());

INSERT INTO t_account (id, user_id, balance, total_recharge, total_consume, created_at, updated_at)
VALUES (2, 2, 200.00, 200.00, 0.00, NOW(), NOW());

INSERT INTO t_account (id, user_id, balance, total_recharge, total_consume, created_at, updated_at)
VALUES (3, 3, 500.00, 500.00, 0.00, NOW(), NOW());

-- ============================================
-- 3. 插入演示书籍
-- ============================================

-- 书籍1：Java核心技术
INSERT INTO t_book (id, title, author, isbn, category, description, page_count, cover_url, content_url, preview_url, status, rating, review_count, borrow_count, hot_score, published_date, created_at, updated_at)
VALUES (1, 'Java核心技术 卷I', 'Cay S. Horstmann', '978-7-111-54742-6', '编程语言', 'Java领域最有影响力和价值的著作之一，拥有20多年教学与研究经验的资深Java技术专家撰写，全球畅销书。', 500, 'https://example.com/covers/java-core.jpg', 'https://example.com/books/java-core/content.epub', 'https://example.com/books/java-core/preview.epub', 'ON_SHELF', 4.8, 120, 350, 95, '2023-01-15', NOW(), NOW());

-- 书籍2：Spring实战
INSERT INTO t_book (id, title, author, isbn, category, description, page_count, cover_url, content_url, preview_url, status, rating, review_count, borrow_count, hot_score, published_date, created_at, updated_at)
VALUES (2, 'Spring实战 第6版', 'Craig Walls', '978-7-115-61234-5', '框架', 'Spring框架学习必备经典，从Spring核心到Spring Boot、响应式编程，全面覆盖现代Java开发。', 450, 'https://example.com/covers/spring-in-action.jpg', 'https://example.com/books/spring-in-action/content.epub', 'https://example.com/books/spring-in-action/preview.epub', 'ON_SHELF', 4.6, 98, 280, 88, '2023-06-01', NOW(), NOW());

-- 书籍3：设计模式
INSERT INTO t_book (id, title, author, isbn, category, description, page_count, cover_url, content_url, preview_url, status, rating, review_count, borrow_count, hot_score, published_date, created_at, updated_at)
VALUES (3, '设计模式：可复用面向对象软件的基础', 'Erich Gamma等', '978-7-111-07575-4', '软件工程', 'GoF经典著作，23种设计模式的权威指南，软件工程师必读经典。', 254, 'https://example.com/covers/design-patterns.jpg', 'https://example.com/books/design-patterns/content.epub', 'https://example.com/books/design-patterns/preview.epub', 'ON_SHELF', 4.9, 200, 500, 99, '2022-03-20', NOW(), NOW());

-- 书籍4：深入理解Java虚拟机
INSERT INTO t_book (id, title, author, isbn, category, description, page_count, cover_url, content_url, preview_url, status, rating, review_count, borrow_count, hot_score, published_date, created_at, updated_at)
VALUES (4, '深入理解Java虚拟机 第3版', '周志明', '978-7-111-64124-7', '编程语言', '全面讲解JVM原理与调优，Java开发者进阶必读，涵盖垃圾回收、内存模型、性能优化等核心主题。', 520, 'https://example.com/covers/jvm.jpg', 'https://example.com/books/jvm/content.epub', 'https://example.com/books/jvm/preview.epub', 'ON_SHELF', 4.7, 150, 420, 92, '2023-09-10', NOW(), NOW());

-- 书籍5：数据结构与算法
INSERT INTO t_book (id, title, author, isbn, category, description, page_count, cover_url, content_url, preview_url, status, rating, review_count, borrow_count, hot_score, published_date, created_at, updated_at)
VALUES (5, '数据结构与算法分析', 'Mark Allen Weiss', '978-7-111-12345-6', '计算机科学', '经典算法教材，从基础数据结构到高级算法设计，配合Java实现，适合计算机专业学生和工程师。', 600, 'https://example.com/covers/algorithms.jpg', 'https://example.com/books/algorithms/content.epub', 'https://example.com/books/algorithms/preview.epub', 'ON_SHELF', 4.5, 85, 200, 80, '2023-02-28', NOW(), NOW());

-- ============================================
-- 4. 插入书籍访问配置
-- ============================================

-- 书籍1：公开免费
INSERT INTO t_book_access_config (id, book_id, access_type, daily_price, vip_discount, max_renew_times, renew_days_free, renew_days_vip, created_at, updated_at)
VALUES (1, 1, 'PUBLIC_FREE', 0.00, 1.00, 2, 7, 15, NOW(), NOW());

-- 书籍2：VIP免费
INSERT INTO t_book_access_config (id, book_id, access_type, daily_price, vip_discount, max_renew_times, renew_days_free, renew_days_vip, created_at, updated_at)
VALUES (2, 2, 'VIP_ONLY', 2.00, 0.50, 3, 7, 15, NOW(), NOW());

-- 书籍3：公开免费
INSERT INTO t_book_access_config (id, book_id, access_type, daily_price, vip_discount, max_renew_times, renew_days_free, renew_days_vip, created_at, updated_at)
VALUES (3, 3, 'PUBLIC_FREE', 0.00, 1.00, 2, 7, 15, NOW(), NOW());

-- 书籍4：付费阅读
INSERT INTO t_book_access_config (id, book_id, access_type, daily_price, vip_discount, max_renew_times, renew_days_free, renew_days_vip, created_at, updated_at)
VALUES (4, 4, 'PAID', 3.00, 0.80, 2, 7, 15, NOW(), NOW());

-- 书籍5：公开免费
INSERT INTO t_book_access_config (id, book_id, access_type, daily_price, vip_discount, max_renew_times, renew_days_free, renew_days_vip, created_at, updated_at)
VALUES (5, 5, 'PUBLIC_FREE', 0.00, 1.00, 2, 7, 15, NOW(), NOW());

-- ============================================
-- 5. 插入演示借阅记录（可选，用于展示已借阅状态）
-- ============================================

-- demo_user 借阅了 Java核心技术（活跃状态）
INSERT INTO t_borrow_record (id, user_id, book_id, borrow_date, due_date, status, renew_count, total_cost, request_id, created_at, updated_at)
VALUES (1, 1, 1, DATEADD(DAY, -3, NOW()), DATEADD(DAY, 4, NOW()), 'ACTIVE', 0, 0.00, 'demo-borrow-001', DATEADD(DAY, -3, NOW()), DATEADD(DAY, -3, NOW()));

-- demo_user 借阅了 设计模式（已归还）
INSERT INTO t_borrow_record (id, user_id, book_id, borrow_date, due_date, return_date, status, renew_count, total_cost, request_id, created_at, updated_at)
VALUES (2, 1, 3, DATEADD(DAY, -10, NOW()), DATEADD(DAY, -3, NOW()), DATEADD(DAY, -4, NOW()), 'RETURNED', 0, 0.00, 'demo-borrow-002', DATEADD(DAY, -10, NOW()), DATEADD(DAY, -4, NOW()));

-- demo_vip 借阅了 Spring实战（活跃状态）
INSERT INTO t_borrow_record (id, user_id, book_id, borrow_date, due_date, status, renew_count, total_cost, request_id, created_at, updated_at)
VALUES (3, 2, 2, DATEADD(DAY, -5, NOW()), DATEADD(DAY, 25, NOW()), 'ACTIVE', 0, 0.00, 'demo-borrow-003', DATEADD(DAY, -5, NOW()), DATEADD(DAY, -5, NOW()));

-- ============================================
-- 6. 重置自增序列（H2数据库需要）
-- ============================================

ALTER TABLE t_user ALTER COLUMN id RESTART WITH 10;
ALTER TABLE t_account ALTER COLUMN id RESTART WITH 10;
ALTER TABLE t_book ALTER COLUMN id RESTART WITH 10;
ALTER TABLE t_book_access_config ALTER COLUMN id RESTART WITH 10;
ALTER TABLE t_borrow_record ALTER COLUMN id RESTART WITH 10;

-- ============================================
-- 验证数据
-- ============================================

-- 查询用户
SELECT id, username, email, user_type, user_status FROM t_user;

-- 查询书籍
SELECT id, title, author, category, status FROM t_book;

-- 查询借阅记录
SELECT id, user_id, book_id, status, borrow_date, due_date FROM t_borrow_record;