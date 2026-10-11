package org.example.sanjiang.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TicketOrder {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long ticketId;
    private String ticketName;
    private String ticketCover;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String status;
    private String verifyCode; // 核销码
    private String refundStatus; // refund_pending, refund_approved, refund_rejected
    private String refundReason;
    private String contactName;
    private String contactPhone;
    private String visitDate;
    private String remark;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
