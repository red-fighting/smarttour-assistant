package com.panduoma.trevaljava.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Long shareId;
    private Long userId;
    private String username;
    private String avatar;
    private String text;
    private LocalDateTime createTime;
}
