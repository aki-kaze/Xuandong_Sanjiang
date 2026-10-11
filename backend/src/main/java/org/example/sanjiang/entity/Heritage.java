package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Heritage {
    private Integer id;
    private String name;
    private String level;
    private String icon;
    private String color;
    private String cover;
    private String description;
    private String detail;
    private String timeline;
    private String skillDesc;
    private String inheritorName;
    private String inheritorTitle;
    private String inheritorStory;
    private String story;
    private String storyAuthor;
    private String relatedTicketIds;
    private String audioUrl;
    private String videoUrl;
    private Double lat;
    private Double lng;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
