package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiKnowledge {
    private Long id;
    private String category; // prompt, knowledge
    private String title;
    private String content;
    private Integer enabled; // 0:禁用, 1:启用
    private Integer sortOrder;
    private Integer version; // 版本号，每次编辑+1
    private String originalContent; // 编辑前的内容快照（用于回滚）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
