package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Food {
    private Integer id;
    private String name;
    private String category;
    private String price;
    private Double rating;
    private String cover;
    private String description;
    private String tags;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
