-- =====================================================
-- 旅游商品表 & 购物车/订单表
-- =====================================================

-- 1. 旅游商品表
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(200) NOT NULL COMMENT '商品标题',
    origin          VARCHAR(100) NOT NULL DEFAULT '' COMMENT '出发地',
    destination     VARCHAR(100) NOT NULL DEFAULT '' COMMENT '目的地',
    duration        VARCHAR(100) NOT NULL DEFAULT '' COMMENT '游玩时间（如"3天2晚"）',
    price           DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '价格（元）',
    image           VARCHAR(500) NOT NULL DEFAULT '' COMMENT '商品主图 URL',
    images          VARCHAR(2000) NOT NULL DEFAULT '[]' COMMENT '详情图片 JSON 数组',
    route_desc      TEXT COMMENT '详细路线介绍',
    food_feature    TEXT COMMENT '美食特色介绍',
    guide_name      VARCHAR(100) NOT NULL DEFAULT '' COMMENT '导游姓名',
    guide_phone     VARCHAR(30) NOT NULL DEFAULT '' COMMENT '导游电话',
    sold_count      INT NOT NULL DEFAULT 0 COMMENT '已售数量',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_destination (destination)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅游商品表';

-- 2. 购物车/订单表（同一张表，用 status 区分：0=购物车，1=已支付，2=已取消）
DROP TABLE IF EXISTS cart_order;
CREATE TABLE cart_order (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL COMMENT '用户 ID',
    product_id      BIGINT NOT NULL COMMENT '商品 ID',
    quantity        INT NOT NULL DEFAULT 1 COMMENT '数量',
    price           DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '下单时价格快照',
    title_snapshot  VARCHAR(200) NOT NULL DEFAULT '' COMMENT '下单时标题快照',
    image_snapshot  VARCHAR(500) NOT NULL DEFAULT '' COMMENT '下单时图片快照',
    status          TINYINT NOT NULL DEFAULT 0 COMMENT '0=购物车，1=已支付，2=已取消',
    paid_time       DATETIME DEFAULT NULL COMMENT '支付时间',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_status (user_id, status),
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车/订单表';

-- =====================================================
-- 插入初始测试数据（九寨沟等 6 个旅游商品）
-- =====================================================
INSERT INTO product (title, origin, destination, duration, price, image, route_desc, food_feature, guide_name, guide_phone, sold_count) VALUES
(
    '九寨沟3日游包住宿来回',
    '成都',
    '四川省 九寨沟',
    '3天2晚',
    1000.00,
    'https://images.unsplash.com/photo-1545569341-9eb8b30979d9?w=800',
    'Day1：成都集合出发 → 都江堰 → 松潘古城入住\nDay2：全天游览九寨沟（树正沟+日则沟+则查洼沟）\nDay3：黄龙景区 → 返回成都散团',
    '品尝四川特色：藏式土火锅、牦牛肉、洋芋糍粑、九寨酸菜面，正宗川味小食随街可选',
    '王导游',
    '138-0000-0001',
    1000
),
(
    '张家界4日游含玻璃栈道',
    '长沙',
    '湖南省 张家界',
    '4天3晚',
    1680.00,
    'https://images.unsplash.com/photo-1537531383496-f4749b8032cf?w=800',
    'Day1：长沙集合 → 张家界入住\nDay2：武陵源-天子山-袁家界（阿凡达取景地）\nDay3：天门山玻璃栈道-99道弯盘山公路\nDay4：金鞭溪十里画廊 → 返回长沙',
    '湖南美食：张家界三下锅、土家腊肉、社饭、蒿子粑粑、麻辣火锅，回味无穷',
    '李导游',
    '138-0000-0002',
    820
),
(
    '杭州西湖+乌镇3日游',
    '上海',
    '浙江省 杭州',
    '3天2晚',
    899.00,
    'https://images.unsplash.com/photo-1591018653367-4e9a57e7db8c?w=800',
    'Day1：上海出发 → 西湖游船-三潭印月-苏堤春晓-雷峰塔\nDay2：乌镇东栅西栅深度游，夜游西栅\nDay3：灵隐寺飞来峰 → 返回上海',
    '舌尖上的杭州：西湖醋鱼、东坡肉、龙井虾仁、叫花童子鸡，乌镇特色糕点定胜糕',
    '张导游',
    '138-0000-0003',
    1560
),
(
    '黄山+宏村3日游',
    '南京',
    '安徽省 黄山',
    '3天2晚',
    1280.00,
    'https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=800',
    'Day1：南京集合 → 宏村古村夜游（中国画里乡村）\nDay2：黄山登峰-迎客松-光明顶-飞来石，观日落云海\nDay3：看日出-西海大峡谷 → 返回南京',
    '徽菜招牌：臭鳜鱼、毛豆腐、黄山烧饼、石耳炖土鸡，一品徽州风味',
    '陈导游',
    '138-0000-0004',
    680
),
(
    '丽江+大理5日深度游',
    '昆明',
    '云南省 丽江',
    '5天4晚',
    2380.00,
    'https://images.unsplash.com/photo-1528164344705-47542687000d?w=800',
    'Day1：昆明集合 → 大理洱海环游\nDay2：大理古城-洋人街-苍山感通索道\nDay3：前往丽江-束河古镇-四方街夜游\nDay4：玉龙雪山-蓝月谷-印象丽江\nDay5：拉市海湿地公园 → 返回昆明',
    '云南必吃：过桥米线、汽锅鸡、腊排骨火锅、鲜花饼、大理乳扇、丽江三文鱼',
    '赵导游',
    '138-0000-0005',
    2340
),
(
    '西安兵马俑+华山4日游',
    '北京',
    '陕西省 西安',
    '4天3晚',
    1599.00,
    'https://images.unsplash.com/photo-1591018653367-4e9a57e7db8c?w=800',
    'Day1：北京出发 → 西安回民街小吃自由逛\nDay2：秦始皇兵马俑-华清池-骊山\nDay3：华山一日游（西峰索道上下）\nDay4：西安古城墙-大雁塔-永兴坊 → 返京',
    '千年古都美食：肉夹馍、羊肉泡馍、凉皮、biangbiang面、甑糕、水盆羊肉',
    '孙导游',
    '138-0000-0006',
    1890
);
