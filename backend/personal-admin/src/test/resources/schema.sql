DROP TABLE IF EXISTS module_usage_log;
DROP TABLE IF EXISTS todo_task;
DROP TABLE IF EXISTS goal_task;
DROP TABLE IF EXISTS goal;
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


