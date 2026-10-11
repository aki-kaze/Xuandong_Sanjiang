package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Product {
    private Integer id;
    private String name;
    private String category;
    private Double price;
    private String cover;
    private String desc;
    private String fullDesc;
    private String tags;
    private Integer sales;
    private Boolean hot;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
