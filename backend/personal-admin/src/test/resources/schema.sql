DROP TABLE IF EXISTS module_usage_log;
DROP TABLE IF EXISTS todo_task;
DROP TABLE IF EXISTS goal_task;
DROP TABLE IF EXISTS goal;
DROP TABLE IF EXISTS health_record;
DROP TABLE IF EXISTS health_standard;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  phone VARCHAR(32) DEFAULT NULL,
  email VARCHAR(128) DEFAULT NULL,
  nickname VARCHAR(64) DEFAULT NULL,
  avatar_url VARCHAR(500) DEFAULT NULL,
  gender TINYINT DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  last_login_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_username (username),
  UNIQUE KEY uk_sys_user_phone (phone),
  UNIQUE KEY uk_sys_user_email (email),
  KEY idx_sys_user_status_created (status, created_at)
);

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  role_code VARCHAR(40) NOT NULL,
  role_name VARCHAR(64) NOT NULL,
  description VARCHAR(255) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_code (role_code)
);

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  role_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_role (user_id, role_id),
  KEY idx_user_role_role (role_id)
);

MERGE INTO sys_role (id, role_code, role_name, description) KEY (id) VALUES
(1, 'USER', '普通用户', '个人生活管理用户'),
(2, 'ADMIN', '管理员', '平台运营管理员'),
(3, 'SUPER_ADMIN', '超级管理员', '系统管理员');

CREATE TABLE IF NOT EXISTS health_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  metric_code VARCHAR(32) NOT NULL,
  metric_value DECIMAL(12,3) NOT NULL,
  unit VARCHAR(24) NOT NULL,
  record_time DATETIME NOT NULL,
  note VARCHAR(500) DEFAULT NULL,
  source VARCHAR(24) NOT NULL DEFAULT 'MANUAL',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_health_user_metric_time (user_id, metric_code, record_time),
  KEY idx_health_user_time (user_id, record_time)
);

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
);

MERGE INTO health_standard (id, metric_code, metric_name, unit, min_value, max_value, applicable_scope, description, status) KEY (id) VALUES
(1, 'STEPS', '每日步数', '步', 6000.000, 12000.000, 'GENERAL', '成年人每日推荐中高强度活动步数参考（非医疗诊断标准）', 1),
(2, 'SLEEP_HOURS', '睡眠时长', '小时', 7.000, 9.000, 'GENERAL', '成年人推荐每日健康睡眠时长参考（非医疗诊断标准）', 1),
(3, 'WATER_ML', '每日饮水', 'ml', 1500.000, 2500.000, 'GENERAL', '成年人日常建议水分补充总量参考（非医疗诊断标准）', 1),
(4, 'WEIGHT', '体重标准', 'kg', 45.000, 85.000, 'GENERAL', '健康生活体格参考区间（非医疗诊断标准）', 1),
(5, 'CALORIES', '饮食摄入热量', 'kcal', 1600.000, 2400.000, 'GENERAL', '常规轻体力劳动每日膳食能量参考（非医疗诊断标准）', 1);

CREATE TABLE IF NOT EXISTS goal (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(120) NOT NULL,
  description TEXT DEFAULT NULL,
  start_date DATE DEFAULT NULL,
  due_date DATE DEFAULT NULL,
  priority TINYINT NOT NULL DEFAULT 2,
  status VARCHAR(24) NOT NULL DEFAULT 'NOT_STARTED',
  progress DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  completed_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_goal_user_status_due (user_id, status, due_date),
  KEY idx_goal_user_created (user_id, created_at)
);

CREATE TABLE IF NOT EXISTS goal_task (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  goal_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(120) NOT NULL,
  description VARCHAR(500) DEFAULT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'TODO',
  due_at DATETIME DEFAULT NULL,
  completed_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_goal_task_goal_sort (goal_id, sort_no),
  KEY idx_goal_task_user_status (user_id, status)
);

CREATE TABLE IF NOT EXISTS todo_task (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  goal_id BIGINT UNSIGNED DEFAULT NULL,
  title VARCHAR(120) NOT NULL,
  description TEXT DEFAULT NULL,
  priority TINYINT NOT NULL DEFAULT 2,
  status VARCHAR(24) NOT NULL DEFAULT 'TODO',
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
);

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
);


