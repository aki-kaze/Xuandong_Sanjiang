package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Activity {
    private Long id;
    private String title;
    private String cover;
    private String description;
    private String location;
    private Double lat;
    private Double lng;
    private String startTime;
    private String endTime;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String status; // 报名中/进行中/已结束
    private Integer isPublished; // 0-下架 1-上架
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
