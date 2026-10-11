package org.example.sanjiang.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long id;
    private String orderNo;
    private Long userId;
    private String type; // ticket, food, hotel, product
    private Long itemId;
    private String itemName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String status; // pending, paid, used, completed, cancelled, refunded
    private String verifyCode; // 核销码
    private LocalDateTime verifyTime; // 核销时间
    private String contactName;
    private String contactPhone;
    private String visitDate;
    private String remark;
    private String payType; // wechat, alipay
    private String transactionId; // 第三方支付流水号
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
