package com.panduoma.trevaljava.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Favorite {
    private Long id;
    private Long userId;
    private Long postId;
    private String postTitle;
    private String postContent;
    private String postImages;
    private String postTag;
    private String postUsername;
    private String postAvatar;
    private String postLocation;
    private LocalDateTime createTime;
}
