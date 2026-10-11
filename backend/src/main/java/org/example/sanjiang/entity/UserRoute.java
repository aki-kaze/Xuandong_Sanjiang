package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserRoute {
    private Integer id;
    private Integer userId;
    private String name;
    private String description;
    private Integer days;
    private String distance;
    private String budget;
    private String daily;
    private String attractions;
    private String hotels;
    private String startDate;
    private String endDate;
    private Integer people;
    private String transport;
    private Integer status;
    private String cover;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
