package org.example.sanjiang.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductOrder {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long productId;
    private String productName;
    private String productCover;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String status;
    private String verifyCode;
    private String refundStatus;
    private String refundReason;
    private String contactName;
    private String contactPhone;
    private String address;
    private String remark;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
