package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiChatSession {
    private Long id;
    private Long userId;
    private String userName;
    private String title;
    private String lastMessage;
    private Integer messageCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
