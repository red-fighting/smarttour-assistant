package com.panduoma.trevaljava.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CartOrder {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;         // 下单时价格快照
    private String titleSnapshot;     // 标题快照
    private String imageSnapshot;     // 图片快照
    private Integer status;           // 0=购物车 1=已支付 2=已取消
    private LocalDateTime paidTime;   // 支付时间
    private LocalDate startDate;      // 出行出发日期
    private LocalDate endDate;        // 出行返回日期
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
