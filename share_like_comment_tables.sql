-- ========== 点赞记录表（按用户隔离的点赞状态） ==========
-- 每个用户对每条分享最多点一次赞（联合主键保证唯一性）
CREATE TABLE IF NOT EXISTS `share_like` (
  `share_id` BIGINT NOT NULL COMMENT '分享ID',
  `user_id` BIGINT NOT NULL COMMENT '点赞用户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`share_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享点赞记录表';

-- ========== 评论表（按分享 + 用户隔离） ==========
CREATE TABLE IF NOT EXISTS `share_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `share_id` BIGINT NOT NULL COMMENT '所属分享ID',
  `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
  `username` VARCHAR(100) DEFAULT NULL COMMENT '评论用户名（冗余，防止改用户名时评论历史丢失）',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '评论用户头像（冗余）',
  `text` TEXT COMMENT '评论内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`),
  KEY `idx_share_id` (`share_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享评论表';
