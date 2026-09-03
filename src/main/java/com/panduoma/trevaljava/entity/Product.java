package com.panduoma.trevaljava.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private String title;
    private String origin;         // 出发地
    private String destination;    // 目的地
    private String duration;       // 游玩时间（如 3天2晚）
    private BigDecimal price;      // 价格
    private String image;          // 主图 URL
    private String images;         // 详情图 JSON 数组
    private String routeDesc;      // 详细路线
    private String foodFeature;    // 美食特色
    private String guideName;      // 导游姓名
    private String guidePhone;     // 导游电话
    private Integer soldCount;     // 已售
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
