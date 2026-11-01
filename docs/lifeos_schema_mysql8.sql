-- LifeOS MySQL 8.0 schema
-- Charset: utf8mb4 | Engine: InnoDB
-- Notes:
-- 1) All user-owned records carry user_id. Enforce ownership in service layer.
-- 2) Logical relationships are indexed; physical FK constraints are intentionally omitted
--    in this first version to keep soft-delete and module lifecycle manageable.
-- 3) Run on a new database. Review default admin bootstrap strategy before production.

CREATE DATABASE IF NOT EXISTS lifeos
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE lifeos;

-- 01. Identity
CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  phone VARCHAR(32) DEFAULT NULL,
  email VARCHAR(128) DEFAULT NULL,
  nickname VARCHAR(64) DEFAULT NULL,
  avatar_url VARCHAR(500) DEFAULT NULL,
  gender TINYINT DEFAULT NULL COMMENT '0 unknown, 1 male, 2 female',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
  last_login_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_username (username),
  UNIQUE KEY uk_sys_user_phone (phone),
  UNIQUE KEY uk_sys_user_email (email),
  KEY idx_sys_user_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账号';

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  role_code VARCHAR(40) NOT NULL,
  role_name VARCHAR(64) NOT NULL,
  description VARCHAR(255) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  role_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_role (user_id, role_id),
  KEY idx_user_role_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

INSERT IGNORE INTO sys_role(role_code, role_name, description) VALUES
('USER', '普通用户', '个人生活管理用户'),
('ADMIN', '管理员', '平台运营管理员'),
('SUPER_ADMIN', '超级管理员', '系统管理员');

-- 02. Health
CREATE TABLE IF NOT EXISTS health_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  metric_code VARCHAR(32) NOT NULL COMMENT 'STEPS/SLEEP_HOURS/WEIGHT/WATER_ML/CALORIES',
  metric_value DECIMAL(12,3) NOT NULL,
  unit VARCHAR(24) NOT NULL,
  record_time DATETIME NOT NULL,
  note VARCHAR(500) DEFAULT NULL,
  source VARCHAR(24) NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL/DEVICE/IMPORT',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_health_user_metric_time (user_id, metric_code, record_time),
  KEY idx_health_user_time (user_id, record_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康指标记录';

CREATE TABLE IF NOT EXISTS health_standard (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  metric_code VARCHAR(32) NOT NULL,
  metric_name VARCHAR(64) NOT NULL,
  unit VARCHAR(24) NOT NULL,
  min_value DECIMAL(12,3) DEFAULT NULL,
  max_value DECIMAL(12,3) DEFAULT NULL,
  applicable_scope VARCHAR(32) NOT NULL DEFAULT 'GENERAL',
  description VARCHAR(500) DEFAULT NULL,
  effective_from DATE DEFAULT NULL,
  effective_to DATE DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_by BIGINT UNSIGNED DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_health_standard_metric_scope (metric_code, applicable_scope, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康指标参考标准';

-- 03. Goals and tasks
CREATE TABLE IF NOT EXISTS goal (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(120) NOT NULL,
  description TEXT DEFAULT NULL,
  start_date DATE DEFAULT NULL,
  due_date DATE DEFAULT NULL,
  priority TINYINT NOT NULL DEFAULT 2 COMMENT '1 high, 2 medium, 3 low',
  status VARCHAR(24) NOT NULL DEFAULT 'NOT_STARTED',
  progress DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  completed_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_goal_user_status_due (user_id, status, due_date),
  KEY idx_goal_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人目标';

CREATE TABLE IF NOT EXISTS goal_task (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  goal_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(120) NOT NULL,
  description VARCHAR(500) DEFAULT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'TODO' COMMENT 'TODO/DONE',
  due_at DATETIME DEFAULT NULL,
  completed_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_goal_task_goal_sort (goal_id, sort_no),
  KEY idx_goal_task_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目标子任务';

CREATE TABLE IF NOT EXISTS todo_task (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  goal_id BIGINT UNSIGNED DEFAULT NULL,
  title VARCHAR(120) NOT NULL,
  description TEXT DEFAULT NULL,
  priority TINYINT NOT NULL DEFAULT 2,
  status VARCHAR(24) NOT NULL DEFAULT 'TODO' COMMENT 'TODO/IN_PROGRESS/DONE/CANCELLED',
  start_at DATETIME DEFAULT NULL,
  due_at DATETIME DEFAULT NULL,
  remind_at DATETIME DEFAULT NULL,
  completed_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_todo_user_status_due (user_id, status, due_at),
  KEY idx_todo_user_start (user_id, start_at),
  KEY idx_todo_goal (goal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日程待办';

-- 04. Pomodoro
CREATE TABLE IF NOT EXISTS pomodoro_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  goal_id BIGINT UNSIGNED DEFAULT NULL,
  todo_id BIGINT UNSIGNED DEFAULT NULL,
  session_type VARCHAR(16) NOT NULL DEFAULT 'FOCUS' COMMENT 'FOCUS/SHORT_BREAK/LONG_BREAK',
  planned_seconds INT UNSIGNED NOT NULL,
  actual_seconds INT UNSIGNED NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING/PAUSED/COMPLETED/ABORTED',
  started_at DATETIME NOT NULL,
  ended_at DATETIME DEFAULT NULL,
  end_reason VARCHAR(24) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_pomodoro_user_started (user_id, started_at),
  KEY idx_pomodoro_user_status (user_id, status),
  KEY idx_pomodoro_goal (goal_id),
  KEY idx_pomodoro_todo (todo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='番茄钟会话记录';

CREATE TABLE IF NOT EXISTS pomodoro_setting (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  focus_duration INT UNSIGNED NOT NULL DEFAULT 25 COMMENT '专注时长(分钟)',
  short_break_duration INT UNSIGNED NOT NULL DEFAULT 5 COMMENT '短休息时长(分钟)',
  long_break_duration INT UNSIGNED NOT NULL DEFAULT 15 COMMENT '长休息时长(分钟)',
  long_break_interval INT UNSIGNED NOT NULL DEFAULT 4 COMMENT '长休息间隔周期(几次专注后长休息)',
  auto_start_breaks TINYINT NOT NULL DEFAULT 0 COMMENT '是否自动开始休息: 0-否, 1-是',
  auto_start_pomodoros TINYINT NOT NULL DEFAULT 0 COMMENT '是否自动开始下一个番茄: 0-否, 1-是',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_pomodoro_setting_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='番茄钟用户偏好配置';

-- 05. Habits
CREATE TABLE IF NOT EXISTS habit (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(500) DEFAULT NULL,
  frequency VARCHAR(16) NOT NULL DEFAULT 'DAILY' COMMENT 'DAILY/WEEKLY',
  target_count INT UNSIGNED NOT NULL DEFAULT 1,
  start_date DATE NOT NULL,
  end_date DATE DEFAULT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/PAUSED/ARCHIVED',
  reminder_time TIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_habit_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='习惯定义';

CREATE TABLE IF NOT EXISTS habit_checkin (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  habit_id BIGINT UNSIGNED NOT NULL,
  checkin_date DATE NOT NULL,
  note VARCHAR(500) DEFAULT NULL,
  is_makeup TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_habit_checkin_date (habit_id, checkin_date),
  KEY idx_habit_checkin_user_date (user_id, checkin_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='习惯打卡';

-- 06. Finance
CREATE TABLE IF NOT EXISTS finance_category (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'NULL means system category',
  category_name VARCHAR(64) NOT NULL,
  category_type VARCHAR(16) NOT NULL COMMENT 'INCOME/EXPENSE',
  icon VARCHAR(64) DEFAULT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_finance_category_scope (user_id, category_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收支分类';

CREATE TABLE IF NOT EXISTS finance_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  category_id BIGINT UNSIGNED DEFAULT NULL,
  record_type VARCHAR(16) NOT NULL COMMENT 'INCOME/EXPENSE',
  amount DECIMAL(12,2) NOT NULL,
  currency_code CHAR(3) NOT NULL DEFAULT 'CNY',
  record_date DATE NOT NULL,
  title VARCHAR(120) DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_finance_user_date_type (user_id, record_date, record_type),
  KEY idx_finance_user_category_date (user_id, category_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收支明细';

-- 07. Personal life records
CREATE TABLE IF NOT EXISTS personal_item (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  item_name VARCHAR(120) NOT NULL,
  category VARCHAR(64) DEFAULT NULL,
  quantity DECIMAL(10,2) NOT NULL DEFAULT 1.00,
  unit VARCHAR(24) DEFAULT NULL,
  storage_location VARCHAR(120) DEFAULT NULL,
  purchase_date DATE DEFAULT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/ARCHIVED',
  remark VARCHAR(500) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_item_user_status (user_id, status),
  KEY idx_item_user_name (user_id, item_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人物品';

CREATE TABLE IF NOT EXISTS item_lending (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  item_id BIGINT UNSIGNED NOT NULL,
  borrower_name VARCHAR(80) NOT NULL,
  lent_at DATETIME NOT NULL,
  expected_return_at DATETIME DEFAULT NULL,
  returned_at DATETIME DEFAULT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'LENT' COMMENT 'LENT/RETURNED/OVERDUE',
  remark VARCHAR(500) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_lending_user_status_due (user_id, status, expected_return_at),
  KEY idx_lending_item (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品借还记录';

CREATE TABLE IF NOT EXISTS memo (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(160) NOT NULL,
  content MEDIUMTEXT DEFAULT NULL,
  tags VARCHAR(500) DEFAULT NULL COMMENT 'MVP comma-separated; normalize if tag search becomes important',
  is_pinned TINYINT NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/ARCHIVED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_memo_user_pinned_updated (user_id, is_pinned, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='备忘录';

CREATE TABLE IF NOT EXISTS diary (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  diary_date DATE NOT NULL,
  title VARCHAR(160) DEFAULT NULL,
  content MEDIUMTEXT NOT NULL,
  mood VARCHAR(24) DEFAULT NULL,
  weather VARCHAR(24) DEFAULT NULL,
  cover_url VARCHAR(500) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_diary_user_date (user_id, diary_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人日记';

-- 08. Reminders
CREATE TABLE IF NOT EXISTS reminder_rule (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  rule_name VARCHAR(100) NOT NULL,
  trigger_type VARCHAR(24) NOT NULL COMMENT 'TODO_DUE/HABIT_CHECKIN/GOAL_DUE/CUSTOM',
  business_ref_id BIGINT UNSIGNED DEFAULT NULL,
  remind_at TIME DEFAULT NULL,
  advance_minutes INT UNSIGNED NOT NULL DEFAULT 0,
  repeat_rule VARCHAR(255) DEFAULT NULL,
  start_date DATE DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_reminder_rule_user_status (user_id, status, trigger_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒规则';

CREATE TABLE IF NOT EXISTS reminder_message (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  rule_id BIGINT UNSIGNED DEFAULT NULL,
  title VARCHAR(160) NOT NULL,
  content VARCHAR(1000) DEFAULT NULL,
  message_type VARCHAR(24) NOT NULL DEFAULT 'SYSTEM',
  business_ref_id BIGINT UNSIGNED DEFAULT NULL,
  scheduled_at DATETIME DEFAULT NULL,
  sent_at DATETIME DEFAULT NULL,
  read_at DATETIME DEFAULT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SENT/READ/FAILED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_reminder_message_user_status_time (user_id, status, scheduled_at),
  KEY idx_reminder_message_rule (rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内提醒消息';

CREATE TABLE IF NOT EXISTS reminder_execution_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  rule_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  idempotency_key VARCHAR(160) NOT NULL,
  execute_at DATETIME NOT NULL,
  result_status VARCHAR(16) NOT NULL DEFAULT 'SUCCESS',
  error_message VARCHAR(500) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_reminder_idempotency (idempotency_key),
  KEY idx_reminder_exec_user_time (user_id, execute_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒执行幂等日志';

-- 09. Module usage analytics
CREATE TABLE IF NOT EXISTS module_usage_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  module_code VARCHAR(32) NOT NULL,
  action_code VARCHAR(48) NOT NULL,
  business_ref_id BIGINT UNSIGNED DEFAULT NULL,
  occurred_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  request_id VARCHAR(64) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_usage_module_time (module_code, occurred_at),
  KEY idx_usage_user_time (user_id, occurred_at),
  KEY idx_usage_action_time (action_code, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模块关键操作记录';

-- Suggested system finance categories (can be adjusted by admin).
INSERT INTO finance_category(user_id, category_name, category_type, icon, sort_no)
SELECT NULL, '餐饮', 'EXPENSE', 'food', 10
WHERE NOT EXISTS (SELECT 1 FROM finance_category WHERE user_id IS NULL AND category_name='餐饮' AND category_type='EXPENSE');
INSERT INTO finance_category(user_id, category_name, category_type, icon, sort_no)
SELECT NULL, '交通', 'EXPENSE', 'transport', 20
WHERE NOT EXISTS (SELECT 1 FROM finance_category WHERE user_id IS NULL AND category_name='交通' AND category_type='EXPENSE');
INSERT INTO finance_category(user_id, category_name, category_type, icon, sort_no)
SELECT NULL, '购物', 'EXPENSE', 'shopping', 30
WHERE NOT EXISTS (SELECT 1 FROM finance_category WHERE user_id IS NULL AND category_name='购物' AND category_type='EXPENSE');
INSERT INTO finance_category(user_id, category_name, category_type, icon, sort_no)
SELECT NULL, '工资', 'INCOME', 'salary', 10
WHERE NOT EXISTS (SELECT 1 FROM finance_category WHERE user_id IS NULL AND category_name='工资' AND category_type='INCOME');
INSERT INTO finance_category(user_id, category_name, category_type, icon, sort_no)
SELECT NULL, '其他收入', 'INCOME', 'income', 90
WHERE NOT EXISTS (SELECT 1 FROM finance_category WHERE user_id IS NULL AND category_name='其他收入' AND category_type='INCOME');

-- Admin bootstrap:
-- Create the first admin with a BCrypt password hash through an application bootstrap script.
-- Do NOT insert a plaintext password here.
