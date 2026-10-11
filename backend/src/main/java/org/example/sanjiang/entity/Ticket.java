package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Ticket {
    private Integer id;
    private String name;
    private String type;
    private Integer price;
    private Integer originalPrice;
    private String cover;
    private Integer validDays;
    private String description;
    private String includes;
    private String notice;
    private String address;
    private Double lat;
    private Double lng;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
