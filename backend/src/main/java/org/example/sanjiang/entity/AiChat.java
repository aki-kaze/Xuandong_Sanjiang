package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiChat {
    private Long id;
    private Long sessionId;
    private Long userId;
    private String userName;
    private String question;
    private String answer;
    private String category; // 交通查询, 路线规划, 美食推荐, 文化咨询, 景点推荐, 其他
    private Integer responseTime; // 响应时间(毫秒)
    private Integer tokens; // 消耗token数
    private Integer hasSensitive; // 0:无敏感词, 1:有敏感词
    private String sensitiveWords; // 检测到的敏感词列表(JSON)
    private Integer flagged; // 0:正常, 1:标记
    private Integer rating; // 0:未评价, 1:不满意, 2:满意
    private String ip;
    private LocalDateTime createTime;
}
