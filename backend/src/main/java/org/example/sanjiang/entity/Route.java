package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Route {
    private Integer id;
    private String name;
    private String duration;
    private String difficulty;
    private String distance;
    private Integer price;
    private String cover;
    private String tags;
    private String stops;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 自定义路线规划字段
    private Integer userId;
    private String description;
    private Integer days;
    private String budget;
    private String daily;
    private String attractions;
    private String hotels;
    private String startDate;
    private String endDate;
    private Integer people;
    private String transport;
}
