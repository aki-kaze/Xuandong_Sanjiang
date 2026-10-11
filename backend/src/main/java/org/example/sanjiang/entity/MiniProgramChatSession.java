package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MiniProgramChatSession {
    private Long id;
    private Long userId;
    private String title;
    private Integer messageCount;
    private LocalDateTime lastMessageAt;
    private Integer status; // 1:正常 0:已删除
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
