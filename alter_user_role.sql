-- 1. 给 user 表加 role 字段：0=普通用户 1=管理员
ALTER TABLE user ADD COLUMN IF NOT EXISTS role TINYINT NOT NULL DEFAULT 0 COMMENT '0=普通用户 1=管理员' AFTER status;

-- 2. 设置 admin/admin1/admin2/admin3 为管理员（username 以 admin 开头）
UPDATE user SET role = 1 WHERE username LIKE 'admin%';

-- 3. 其他用户保持默认 0
