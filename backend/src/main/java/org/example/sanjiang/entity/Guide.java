package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Guide {
    private Long id;
    private String title;
    private String desc;
    private String content;
    private String author;
    private String authorAvatar;
    private String cover;
    private Long views;
    private String tags;       // JSON array
    private String tips;       // JSON array
    private String itinerary;  // JSON array
    private Integer hot;       // 0=否，1=是
    private Integer status;    // 0=待审核，1=已通过，2=已拒绝
    private Integer auditStatus; // 审核状态：0=待审核，1=已通过，2=已拒绝
    private Long userId;       // 发布用户 ID
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
