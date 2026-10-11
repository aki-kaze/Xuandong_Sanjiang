package org.example.sanjiang.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FoodOrder {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long foodId;
    private String foodName;
    private String foodCover;
    private String shopName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String status;
    private String verifyCode;
    private String refundStatus;
    private String refundReason;
    private String contactName;
    private String contactPhone;
    private String remark;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
