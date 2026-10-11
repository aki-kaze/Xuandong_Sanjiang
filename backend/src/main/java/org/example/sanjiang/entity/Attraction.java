package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Attraction {
    private Integer id;
    private String name;
    private String category;
    private Double rating;
    private Integer price;
    private String address;
    private Double lat;
    private Double lng;
    private String cover;
    private String images;
    private String tags;
    private String description;
    private String openTime;
    private String phone;
    private String tips;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
