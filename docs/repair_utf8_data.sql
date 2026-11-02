-- =================================================================
-- LifeOS 中文乱码快速修复脚本 (Repair Mojibake / Latin-1 UTF-8 Data)
-- 适用场景：
--   若不想销毁 Docker Volume，可直接在 Adminer (http://localhost:8088)
--   或 MySQL 客户端中直接运行此脚本，一键将乱码数据修正为正确中文。
-- =================================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE lifeos;

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 0. 结构修复：确保提醒模块表包含 deleted 逻辑删除字段 (解决 Unknown column 'deleted' 报错)
SET @col_rule = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reminder_rule' AND COLUMN_NAME = 'deleted');
SET @sql_rule = IF(@col_rule = 0, 'ALTER TABLE reminder_rule ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0;', 'SELECT 1;');
PREPARE stmt_rule FROM @sql_rule;
EXECUTE stmt_rule;
DEALLOCATE PREPARE stmt_rule;

SET @col_msg = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reminder_message' AND COLUMN_NAME = 'deleted');
SET @sql_msg = IF(@col_msg = 0, 'ALTER TABLE reminder_message ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0;', 'SELECT 1;');
PREPARE stmt_msg FROM @sql_msg;
EXECUTE stmt_msg;
DEALLOCATE PREPARE stmt_msg;

-- 1. 修复角色字典表
INSERT INTO sys_role (role_code, role_name, description) VALUES
('USER', '普通用户', '个人生活管理用户'),
('ADMIN', '管理员', '平台运营管理员'),
('SUPER_ADMIN', '超级管理员', '系统管理员')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), description = VALUES(description);

-- 2. 修复基础财务分类表
DELETE FROM finance_category WHERE user_id IS NULL;
INSERT INTO finance_category(user_id, category_name, category_type, icon, sort_no) VALUES
(NULL, '餐饮', 'EXPENSE', 'food', 10),
(NULL, '交通', 'EXPENSE', 'transport', 20),
(NULL, '购物', 'EXPENSE', 'shopping', 30),
(NULL, '工资', 'INCOME', 'salary', 10),
(NULL, '其他收入', 'INCOME', 'income', 90);

-- 3. 修复健康指标标准
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

-- 4. 修复系统用户昵称
UPDATE sys_user SET nickname = '系统管理员' WHERE id = 1 AND username = 'admin';
UPDATE sys_user SET nickname = '极客小明' WHERE id = 2 AND username = 'demo';

-- 5. 修复待办任务数据
UPDATE todo_task SET title = '完成 LifeOS 个人生活管理系统架构验证', description = '验证待办、目标、健康、记账与番茄钟全流程闭环' WHERE id = 1;
UPDATE todo_task SET title = '每周坚持户外慢跑 10 公里', description = '增强心肺功能，记录在健康模块' WHERE id = 2;
UPDATE todo_task SET title = '整理本月家庭收支与财务明细', description = '核对餐饮、交通与数码支出' WHERE id = 3;
UPDATE todo_task SET title = '精读《微服务设计模式》核心章节', description = '关注数据一致性与领域驱动设计' WHERE id = 4;

-- 6. 修复目标与子任务
UPDATE goal SET title = '2026 年度个人全栈工程师技能跃迁', description = '涵盖 Spring Boot 3、Vue 3、微服务与云原生生产部署' WHERE id = 1;
UPDATE goal_task SET title = '掌握 Spring Boot 3 与 JDK 21 虚拟线程核心特性' WHERE id = 1;
UPDATE goal_task SET title = '熟练运用 Vue 3 + TypeScript 组合式 API 与 Pinia 状态管理' WHERE id = 2;
UPDATE goal_task SET title = '完成 uni-app 跨端小程序与 H5 响应式适配' WHERE id = 3;
UPDATE goal_task SET title = '掌握 Docker Compose 容器编排与生产反向代理配置' WHERE id = 4;

-- 7. 修复习惯打卡与记录
UPDATE habit SET name = '早起晨读 30 分钟', description = '每日早晨 7:30 阅读专业书籍或技术周刊' WHERE id = 1;
UPDATE habit SET name = '每日饮水 2000ml', description = '规律补水，促进新陈代谢' WHERE id = 2;

-- 8. 修复个人物品、借还、备忘录与日记
UPDATE personal_item SET item_name = 'Sony WH-1000XM5 无线降噪耳机', category = '数码装备', unit = '件', storage_location = '书房书架顶层', remark = '长途办公与沉浸专注必备' WHERE id = 1;
UPDATE personal_item SET item_name = '《深入理解 Java 虚拟机》第3版', category = '纸质书籍', unit = '本', storage_location = '书桌左侧书架', remark = '已被同事借走借阅' WHERE id = 2;
UPDATE item_lending SET borrower_name = '同事张工', remark = '借阅复习垃圾回收机制' WHERE id = 1;
UPDATE memo SET title = '家庭网络与主力设备 IP 分配备忘', content = '路由器：192.168.1.1\nNAS 存储：192.168.1.200\n本地开发机：192.168.1.105', tags = '家庭网络,设备' WHERE id = 1;
UPDATE memo SET title = '极简个人生活物资清单', content = '定期清理不常用的闲置物品，保持办公桌面与生活空间整洁。', tags = '生活习惯,整理' WHERE id = 2;
UPDATE diary SET title = '生活操作系统 LifeOS 顺利上线', content = '今天把所有模块（待办、目标、健康、番茄钟、打卡与记账）全部梳理完毕，系统运行非常丝滑，对未来的生活节奏充满掌控感与期待！' WHERE id = 1;

-- 9. 修复智能提醒与消息通知
UPDATE reminder_rule SET rule_name = '每日晨间阅读提醒' WHERE id = 1;
UPDATE reminder_rule SET rule_name = '待办事项临近截止提醒' WHERE id = 2;
UPDATE reminder_message SET title = '【待办提醒】您的任务「整理本月家庭收支与财务明细」即将截止', content = '请及时处理并核对账单信息，保持财务清晰' WHERE id = 1;
UPDATE reminder_message SET title = '【欢迎使用】欢迎体验 LifeOS 个人生活管理系统！', content = '您已成功进入 LifeOS，系统已为您准备好示例数据供体验浏览。' WHERE id = 2;

-- 10. 修复健康与财务备注
DELETE FROM health_record WHERE user_id = 2;
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

DELETE FROM finance_record WHERE user_id = 2;
INSERT INTO finance_record (user_id, category_id, record_type, amount, record_date, remark, created_at, updated_at, deleted)
VALUES
(2, 4, 'INCOME',  18500.00, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '本月薪资所得', NOW(), NOW(), 0),
(2, 5, 'INCOME',   1200.00, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '理财收益到账', NOW(), NOW(), 0),
(2, 1, 'EXPENSE',   128.50, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '周末家庭聚餐', NOW(), NOW(), 0),
(2, 2, 'EXPENSE',    45.00, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '地铁通勤充值', NOW(), NOW(), 0),
(2, 3, 'EXPENSE',   399.00, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '购买机械键盘', NOW(), NOW(), 0),
(2, 1, 'EXPENSE',    32.00, CURDATE(), '午餐简餐与咖啡', NOW(), NOW(), 0);

