package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Journey {
    private Long id;
    private Long userId;
    private String name;
    private String cover;
    private String city;
    private Integer days;
    private Integer people;
    private String startDate;
    private String endDate;
    private String status;
    private String notes;
    private String attractionIds;
    private String stops;
    private String hotelInfo;
    private String costBreakdown;
    private Integer estimatedCost;
    private Boolean isCustom;
    private String tags;
    private String difficulty;
    private String distance;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
