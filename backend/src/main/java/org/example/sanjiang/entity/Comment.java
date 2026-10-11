package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Long postId;
    private Long userId;
    private String nickname;
    private String avatar;
    private String content;
    private String images;
    private Integer likes;
    private Integer status; // 0:待审核, 1:已通过, 2:已拒绝
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
