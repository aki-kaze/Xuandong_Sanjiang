package org.example.sanjiang.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExperienceOrder {
    private Long id;
    private String orderNo;
    private Long userId;
    private Integer heritageId;
    private String heritageName;
    private String heritageCover;
    private String experienceType;   // 体验类型：singing(大歌), bridge(风雨桥), drum(鼓楼), weaving(侗锦), lusheng(芦笙), duoye(多耶)
    private Integer quantity;        // 预约人数
    private BigDecimal price;        // 单价
    private BigDecimal totalPrice;   // 总价
    private String status;           // pending, confirmed, completed, cancelled
    private String contactName;
    private String contactPhone;
    private String visitDate;        // 预约日期
    private String visitTime;        // 预约时段：morning, afternoon, evening
    private String remark;
    private String cancelReason;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
