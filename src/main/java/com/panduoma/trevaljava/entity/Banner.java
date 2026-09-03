package com.panduoma.trevaljava.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 首页轮播图。
 * image_url 为可直接访问的 URL（如 /uploads/image1.png 或外链）。
 */
@Data
public class Banner {
  private Long id;
  private String title; // 主标题
  private String subtitle; // 副标题
  private String imageUrl; // 图片 URL
  private Integer sortOrder; // 排序（越小越靠前）
  private Integer status; // 1 启用 0 禁用
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
