-- =================================================================
-- LifeOS 本地开发与演示环境初始测试数据 (Seed Data)
-- 包含：
--   1. 管理员账号 (admin / admin123) 与 普通演示账号 (demo / demo123)
--   2. 待办任务、目标与子任务
--   3. 健康多维记录与健康标准
--   4. 专注番茄钟会话记录
--   5. 习惯打卡与连续签到历史
--   6. 收支明细与分类账单
--   7. 个人闲置物品、借还记录、置顶备忘录、私密心情日记
--   8. 智能提醒规则与站内消息
--   9. 模块使用审计日志（用于点亮管理端看板大屏）
-- =================================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE lifeos;

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- -------------------------------------------------------------
-- 1. 用户账号初始化 (密码均为 BCrypt 强哈希)
--    admin -> admin123
--    demo  -> demo123
-- -------------------------------------------------------------
INSERT INTO sys_user (id, username, password_hash, phone, email, nickname, avatar_url, gender, status, created_at, updated_at, deleted)
VALUES
(1, 'admin', '$2a$10$GXKuAj/SVI6OyupiKPrriOBCfV/Yd9fKwuq2YMpLkQsjotFIf/JBe', '13800000001', 'admin@lifeos.local', '系统管理员', 'https://api.dicebear.com/7.x/bottts/svg?seed=admin', 1, 1, NOW(), NOW(), 0),
(2, 'demo',  '$2a$10$uuhraaRyHW18UDYtR4znK.to5icvyLmHg2Xq8bCQW6qnYHVD6uFkK', '13800000002', 'demo@lifeos.local',  '极客小明',   'https://api.dicebear.com/7.x/avataaars/svg?seed=demo', 1, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  nickname = VALUES(nickname),
  avatar_url = VALUES(avatar_url),
  updated_at = NOW();

-- 绑定角色权限
-- admin -> ROLE_ADMIN, ROLE_USER
-- demo  -> ROLE_USER
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT 1, id FROM sys_role WHERE role_code = 'ADMIN';

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT 1, id FROM sys_role WHERE role_code = 'USER';

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT 2, id FROM sys_role WHERE role_code = 'USER';

-- -------------------------------------------------------------
-- 2. 待办事项 (Todo) - demo 用户
-- -------------------------------------------------------------
INSERT INTO todo_task (id, user_id, goal_id, title, description, priority, status, start_at, due_at, remind_at, completed_at, created_at, updated_at, deleted)
VALUES
(1, 2, NULL, '完成 LifeOS 个人生活管理系统架构验证', '验证待办、目标、健康、记账与番茄钟全流程闭环', 1, 'DONE', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW(), NULL, NOW(), DATE_SUB(NOW(), INTERVAL 2 DAY), NOW(), 0),
(2, 2, 1,    '每周坚持户外慢跑 10 公里', '增强心肺功能，记录在健康模块', 2, 'IN_PROGRESS', NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY), NULL, NULL, NOW(), NOW(), 0),
(3, 2, NULL, '整理本月家庭收支与财务明细', '核对餐饮、交通与数码支出', 2, 'TODO', NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), NULL, NOW(), NOW(), 0),
(4, 2, 1,    '精读《微服务设计模式》核心章节', '关注数据一致性与领域驱动设计', 3, 'TODO', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), NULL, NULL, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  description = VALUES(description),
  updated_at = NOW();

-- -------------------------------------------------------------
-- 3. 目标与子任务 (Goal & Tasks) - demo 用户
-- -------------------------------------------------------------
INSERT INTO goal (id, user_id, title, description, start_date, due_date, priority, status, progress, completed_at, created_at, updated_at, deleted)
VALUES
(1, 2, '2026 年度个人全栈工程师技能跃迁', '涵盖 Spring Boot 3、Vue 3、微服务与云原生生产部署', DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 80 DAY), 1, 'IN_PROGRESS', 50.00, NULL, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW(), 0)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  description = VALUES(description),
  progress = VALUES(progress),
  updated_at = NOW();

INSERT INTO goal_task (id, goal_id, user_id, title, status, sort_no, completed_at, created_at, updated_at, deleted)
VALUES
(1, 1, 2, '掌握 Spring Boot 3 与 JDK 21 虚拟线程核心特性', 'DONE', 1, NOW(), DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 0),
(2, 1, 2, '熟练运用 Vue 3 + TypeScript 组合式 API 与 Pinia 状态管理', 'DONE', 2, NOW(), DATE_SUB(NOW(), INTERVAL 3 DAY), NOW(), 0),
(3, 1, 2, '完成 uni-app 跨端小程序与 H5 响应式适配', 'TODO', 3, NULL, NOW(), NOW(), 0),
(4, 1, 2, '掌握 Docker Compose 容器编排与生产反向代理配置', 'TODO', 4, NULL, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  updated_at = NOW();

-- -------------------------------------------------------------
-- 4. 健康记录与参考标准 (Health) - demo 用户
-- -------------------------------------------------------------
INSERT INTO health_standard (metric_code, metric_name, unit, min_value, max_value, applicable_scope, description, effective_from, status)
VALUES
('STEPS', '每日步数', '步', 6000.0, 15000.0, 'GENERAL', '成年人日常健康推荐运动步数', '2026-01-01', 1),
('WEIGHT', '身体体重', 'kg', 50.0, 75.0, 'GENERAL', '标准BMI健康体重范围', '2026-01-01', 1),
('WATER_ML', '每日饮水量', 'ml', 1500.0, 2500.0, 'GENERAL', '人体基础水分代谢需求', '2026-01-01', 1),
('SLEEP_HOURS', '每日睡眠', '小时', 7.0, 9.0, 'GENERAL', '成年人深度修复推荐睡眠时长', '2026-01-01', 1)
ON DUPLICATE KEY UPDATE
  metric_name = VALUES(metric_name),
  unit = VALUES(unit),
  description = VALUES(description);

INSERT INTO health_record (user_id, metric_code, metric_value, unit, record_time, note, source, created_at, updated_at, deleted)
VALUES
(2, 'STEPS', 8200.000, '步', DATE_SUB(NOW(), INTERVAL 3 DAY), '公园慢跑', 'DEVICE', NOW(), NOW(), 0),
(2, 'STEPS', 10500.000, '步', DATE_SUB(NOW(), INTERVAL 2 DAY), '达标万步', 'DEVICE', NOW(), NOW(), 0),
(2, 'STEPS', 9300.000, '步', DATE_SUB(NOW(), INTERVAL 1 DAY), '日常散步', 'DEVICE', NOW(), NOW(), 0),
(2, 'WEIGHT', 65.500, 'kg', DATE_SUB(NOW(), INTERVAL 3 DAY), '早晨空腹称重', 'MANUAL', NOW(), NOW(), 0),
(2, 'WEIGHT', 65.200, 'kg', NOW(), '晨起称重，体脂稳定', 'MANUAL', NOW(), NOW(), 0),
(2, 'WATER_ML', 2000.000, 'ml', DATE_SUB(NOW(), INTERVAL 1 DAY), '8杯温开水', 'MANUAL', NOW(), NOW(), 0),
(2, 'WATER_ML', 2200.000, 'ml', NOW(), '保持充足水合', 'MANUAL', NOW(), NOW(), 0),
(2, 'SLEEP_HOURS', 7.800, '小时', DATE_SUB(NOW(), INTERVAL 1 DAY), '深度睡眠质量良好', 'DEVICE', NOW(), NOW(), 0),
(2, 'SLEEP_HOURS', 8.100, '小时', NOW(), '精力充沛的一天', 'DEVICE', NOW(), NOW(), 0);

-- -------------------------------------------------------------
-- 5. 专注番茄钟 (Pomodoro) - demo 用户
-- -------------------------------------------------------------
INSERT INTO pomodoro_setting (user_id, focus_duration, short_break_duration, long_break_duration, long_break_interval, auto_start_breaks, auto_start_pomodoros)
VALUES (2, 25, 5, 15, 4, 1, 0)
ON DUPLICATE KEY UPDATE focus_duration = 25;

INSERT INTO pomodoro_record (user_id, goal_id, todo_id, session_type, planned_seconds, actual_seconds, status, started_at, ended_at, end_reason, created_at, updated_at)
VALUES
(2, 1, 1, 'FOCUS', 1500, 1500, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 95 MINUTE), 'NORMAL_FINISH', NOW(), NOW()),
(2, 1, 2, 'FOCUS', 1500, 1500, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 35 MINUTE), 'NORMAL_FINISH', NOW(), NOW());

-- -------------------------------------------------------------
-- 6. 习惯打卡 (Habit) - demo 用户
-- -------------------------------------------------------------
INSERT INTO habit (id, user_id, name, description, frequency, target_count, start_date, status, created_at, updated_at, deleted)
VALUES
(1, 2, '早起晨读 30 分钟', '每日早晨 7:30 阅读专业书籍或技术周刊', 'DAILY', 1, DATE_SUB(CURDATE(), INTERVAL 20 DAY), 'ACTIVE', NOW(), NOW(), 0),
(2, 2, '每日饮水 2000ml', '规律补水，促进新陈代谢', 'DAILY', 1, DATE_SUB(CURDATE(), INTERVAL 25 DAY), 'ACTIVE', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  updated_at = NOW();

-- 习惯打卡历史（最近几天）
INSERT IGNORE INTO habit_checkin (habit_id, user_id, checkin_date, note, is_makeup, created_at)
VALUES
(1, 2, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '完成《微服务设计》第3章', 0, NOW()),
(1, 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '阅读 Vue 3 源码分析文章', 0, NOW()),
(1, 2, CURDATE(), '晨读专栏《深入理解 JVM 垃圾回收》', 0, NOW()),
(2, 2, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '打卡第 6 杯水', 0, NOW()),
(2, 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '喝水 2100ml 达标', 0, NOW()),
(2, 2, CURDATE(), '全天目标达成', 0, NOW());

-- -------------------------------------------------------------
-- 7. 收支账单 (Finance) - demo 用户
-- -------------------------------------------------------------
INSERT INTO finance_record (user_id, category_id, record_type, amount, record_date, remark, created_at, updated_at, deleted)
VALUES
(2, 4, 'INCOME',  18500.00, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '本月薪资所得', NOW(), NOW(), 0),
(2, 5, 'INCOME',   1200.00, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '理财收益到账', NOW(), NOW(), 0),
(2, 1, 'EXPENSE',   128.50, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '周末家庭聚餐', NOW(), NOW(), 0),
(2, 2, 'EXPENSE',    45.00, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '地铁通勤充值', NOW(), NOW(), 0),
(2, 3, 'EXPENSE',   399.00, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '购买机械键盘', NOW(), NOW(), 0),
(2, 1, 'EXPENSE',    32.00, CURDATE(), '午餐简餐与咖啡', NOW(), NOW(), 0);

-- -------------------------------------------------------------
-- 8. 生活记录 (Item, Memo, Diary) - demo 用户
-- -------------------------------------------------------------
INSERT INTO personal_item (id, user_id, item_name, category, quantity, unit, storage_location, purchase_date, status, remark, created_at, updated_at, deleted)
VALUES
(1, 2, 'Sony WH-1000XM5 无线降噪耳机', '数码装备', 1.00, '件', '书房书架顶层', DATE_SUB(CURDATE(), INTERVAL 60 DAY), 'ACTIVE', '长途办公与沉浸专注必备', NOW(), NOW(), 0),
(2, 2, '《深入理解 Java 虚拟机》第3版', '纸质书籍', 1.00, '本', '书桌左侧书架', DATE_SUB(CURDATE(), INTERVAL 120 DAY), 'ACTIVE', '已被同事借走借阅', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  item_name = VALUES(item_name),
  category = VALUES(category),
  storage_location = VALUES(storage_location),
  remark = VALUES(remark),
  updated_at = NOW();

INSERT INTO item_lending (id, item_id, user_id, borrower_name, lent_at, expected_return_at, returned_at, status, remark, created_at, updated_at)
VALUES
(1, 2, 2, '同事张工', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), NULL, 'LENT', '借阅复习垃圾回收机制', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  borrower_name = VALUES(borrower_name),
  remark = VALUES(remark),
  updated_at = NOW();

INSERT INTO memo (id, user_id, title, content, tags, is_pinned, status, created_at, updated_at, deleted)
VALUES
(1, 2, '家庭网络与主力设备 IP 分配备忘', '路由器：192.168.1.1\nNAS 存储：192.168.1.200\n本地开发机：192.168.1.105', '家庭网络,设备', 1, 'ACTIVE', NOW(), NOW(), 0),
(2, 2, '极简个人生活物资清单', '定期清理不常用的闲置物品，保持办公桌面与生活空间整洁。', '生活习惯,整理', 0, 'ACTIVE', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  content = VALUES(content),
  tags = VALUES(tags),
  updated_at = NOW();

INSERT INTO diary (id, user_id, diary_date, title, content, mood, weather, cover_url, created_at, updated_at, deleted)
VALUES
(1, 2, CURDATE(), '生活操作系统 LifeOS 顺利上线', '今天把所有模块（待办、目标、健康、番茄钟、打卡与记账）全部梳理完毕，系统运行非常丝滑，对未来的生活节奏充满掌控感与期待！', 'HAPPY', 'SUNNY', NULL, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  content = VALUES(content),
  updated_at = NOW();

-- -------------------------------------------------------------
-- 9. 智能提醒与站内信 (Reminders) - demo 用户
-- -------------------------------------------------------------
INSERT INTO reminder_rule (id, user_id, rule_name, trigger_type, business_ref_id, remind_at, advance_minutes, repeat_rule, status, created_at, updated_at, deleted)
VALUES
(1, 2, '每日晨间阅读提醒', 'HABIT_CHECKIN', 1, '07:30:00', 0, 'DAILY', 1, NOW(), NOW(), 0),
(2, 2, '待办事项临近截止提醒', 'TODO_DUE', 3, '18:00:00', 30, 'DAILY', 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  rule_name = VALUES(rule_name),
  updated_at = NOW();

INSERT INTO reminder_message (id, user_id, rule_id, title, content, message_type, business_ref_id, status, scheduled_at, sent_at, read_at, created_at, deleted)
VALUES
(1, 2, 2, '【待办提醒】您的任务「整理本月家庭收支与财务明细」即将截止', '请及时处理并核对账单信息，保持财务清晰', 'TODO', 3, 'PENDING', NOW(), NOW(), NULL, NOW(), 0),
(2, 2, 1, '【欢迎使用】欢迎体验 LifeOS 个人生活管理系统！', '您已成功进入 LifeOS，系统已为您准备好示例数据供体验浏览。', 'SYSTEM', NULL, 'READ', NOW(), NOW(), NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  content = VALUES(content),
  status = 'PENDING';

-- -------------------------------------------------------------
-- 10. 模块使用日志 (Module Usage Logs) - 用于点亮管理端看板
-- -------------------------------------------------------------
INSERT INTO module_usage_log (user_id, module_code, action_code, business_ref_id, occurred_at, request_id)
VALUES
(2, 'TODO', 'CREATE', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 'seed-req-001'),
(2, 'TODO', 'COMPLETE', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 'seed-req-002'),
(2, 'GOAL', 'CREATE', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), 'seed-req-003'),
(2, 'HEALTH', 'ADD_STEPS', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), 'seed-req-004'),
(2, 'POMODORO', 'FINISH', 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'seed-req-005'),
(2, 'HABIT', 'CHECKIN', 1, CURDATE(), 'seed-req-006'),
(2, 'FINANCE', 'RECORD_EXPENSE', 3, DATE_SUB(NOW(), INTERVAL 1 DAY), 'seed-req-007'),
(2, 'DIARY', 'CREATE', 1, CURDATE(), 'seed-req-008');
