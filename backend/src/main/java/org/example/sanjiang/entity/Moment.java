package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Moment {
    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private String content;
    private String coverImg;
    private String images;
    private String tags;
    private Integer likes;
    private Integer comments;
    private Integer status; // 0:待审核, 1:已发布
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
