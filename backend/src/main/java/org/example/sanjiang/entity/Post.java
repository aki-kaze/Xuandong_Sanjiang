package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Post {
    private Long id;
    private Long userId;
    private String author;
    private String avatar;
    private String content;
    private String coverImg;
    private String images; // JSON数组，存储多张图片
    private Integer likes;
    private Integer comments;
    private String tags; // JSON数组
    private Integer status; // 1已发布, 0待审核
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 非数据库字段
    private String nickname;
}
