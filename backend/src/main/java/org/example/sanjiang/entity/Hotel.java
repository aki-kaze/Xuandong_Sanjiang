package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Hotel {
    private Integer id;
    private String name;
    private String type;
    private Double rating;
    private Integer price;
    private String address;
    private String cover;
    private String tags;
    private String description;
    private String facilities;
    private Double lat;
    private Double lng;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
