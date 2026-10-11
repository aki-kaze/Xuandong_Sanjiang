package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MiniProgramChatMessage {
    private Long id;
    private Long sessionId;
    private String role; // user, ai
    private String content;
    private String toolType; // attraction, food, route, itinerary, weather, etc.
    private String toolResult; // JSON
    private String itinerary; // JSON
    private String extraData; // JSON
    private Integer msgOrder;
    private LocalDateTime createdAt;
}
