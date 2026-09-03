-- 分享表（社区发帖）
CREATE TABLE IF NOT EXISTS `share` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '发布用户ID',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '标题',
  `content` TEXT COMMENT '内容',
  `images` VARCHAR(2000) DEFAULT NULL COMMENT '图片URL列表（JSON数组字符串）',
  `tag` VARCHAR(20) DEFAULT '攻略' COMMENT '标签：攻略/游记/问答/晒图',
  `likes` INT DEFAULT 0 COMMENT '点赞数',
  `comments` INT DEFAULT 0 COMMENT '评论数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区分享表';

-- 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '收藏用户ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `post_title` VARCHAR(200) DEFAULT NULL COMMENT '帖子标题（冗余）',
  `post_content` TEXT COMMENT '帖子内容（冗余）',
  `post_images` VARCHAR(2000) DEFAULT NULL COMMENT '帖子图片（冗余）',
  `post_tag` VARCHAR(20) DEFAULT NULL COMMENT '帖子标签（冗余）',
  `post_username` VARCHAR(100) DEFAULT NULL COMMENT '发帖用户名（冗余）',
  `post_avatar` VARCHAR(500) DEFAULT NULL COMMENT '发帖用户头像（冗余）',
  `post_location` VARCHAR(100) DEFAULT NULL COMMENT '发帖位置（冗余）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';
