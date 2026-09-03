package com.panduoma.trevaljava.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Share {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String images;
    private String tag;
    private Integer likes;
    private Integer comments;
    private LocalDateTime createTime;
}
