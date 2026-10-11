package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SensitiveWord {
    private Long id;
    private String word;
    private String category; //政治, 色情, 暴力, 广告, 其他
    private Integer level; // 1:轻度, 2:中度, 3:严重
    private Integer hitCount; // 命中次数
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime lastHit; // 最后命中时间
}
