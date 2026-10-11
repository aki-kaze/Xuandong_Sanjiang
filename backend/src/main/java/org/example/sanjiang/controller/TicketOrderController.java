package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.TicketOrder;
import org.example.sanjiang.service.TicketOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ticket-orders")
@CrossOrigin
public class TicketOrderController {

    @Autowired
    private TicketOrderService ticketOrderService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String searchKey,
            @RequestParam(required = false) Long userId) {
        List<TicketOrder> list;
        int total;
        if (userId != null) {
            list = ticketOrderService.findByUserId(userId, page, size);
            total = ticketOrderService.countByUserId(userId);
        } else {
            list = ticketOrderService.findAll(page, size, status, searchKey);
            total = ticketOrderService.count(status, searchKey);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/user/{userId}")
    public Result<List<TicketOrder>> getByUserId(@PathVariable Long userId) {
        return Result.success(ticketOrderService.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public Result<TicketOrder> getById(@PathVariable Long id) {
        TicketOrder order = ticketOrderService.findById(id);
        return Result.success(order);
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> data) {
        TicketOrder order = new TicketOrder();
        if (data.get("orderNo") != null) order.setOrderNo(data.get("orderNo").toString());
        order.setUserId(data.get("userId") != null ? Long.parseLong(data.get("userId").toString()) : 0L);
        order.setTicketId(data.get("ticketId") != null ? Long.parseLong(data.get("ticketId").toString()) : 0L);
        order.setTicketName(data.getOrDefault("ticketName", "").toString());
        order.setTicketCover(data.getOrDefault("ticketCover", "").toString());
        order.setQuantity(data.get("quantity") != null ? Integer.parseInt(data.get("quantity").toString()) : 1);
        if (data.get("price") != null) order.setPrice(new java.math.BigDecimal(data.get("price").toString()));
        if (data.get("totalPrice") != null) order.setTotalPrice(new java.math.BigDecimal(data.get("totalPrice").toString()));
        order.setContactName(data.getOrDefault("contactName", "").toString());
        order.setContactPhone(data.getOrDefault("contactPhone", "").toString());
        if (data.get("visitDate") != null) order.setVisitDate(data.get("visitDate").toString());
        order.setRemark(data.getOrDefault("remark", "").toString());
        Map<String, Object> result = ticketOrderService.create(order);
        return Result.success(result);
    }

    @PostMapping("/pay")
    public Result<String> pay(@RequestParam String orderNo) {
        boolean success = ticketOrderService.pay(orderNo);
        return success ? Result.success("支付成功") : Result.error("支付失败");
    }

    @PostMapping("/cancel")
    public Result<String> cancel(@RequestParam String orderNo) {
        boolean success = ticketOrderService.cancel(orderNo);
        return success ? Result.success("取消成功") : Result.error("取消失败");
    }

    @PostMapping("/use")
    public Result<String> use(@RequestParam String orderNo) {
        boolean success = ticketOrderService.use(orderNo);
        return success ? Result.success("核销成功") : Result.error("核销失败");
    }

    @PostMapping("/refund/apply")
    public Result<String> applyRefund(@RequestParam String orderNo, @RequestParam(required = false) String reason) {
        TicketOrder order = ticketOrderService.findByOrderNo(orderNo);
        if (order == null) return Result.error("订单不存在");
        order.setRefundStatus("refund_pending");
        order.setRefundReason(reason);
        ticketOrderService.update(order);
        return Result.success("退款申请已提交");
    }

    @PostMapping("/refund/approve")
    public Result<String> approveRefund(@RequestParam String orderNo) {
        TicketOrder order = ticketOrderService.findByOrderNo(orderNo);
        if (order == null) return Result.error("订单不存在");
        order.setRefundStatus("refund_approved");
        order.setStatus("refunded");
        ticketOrderService.update(order);
        return Result.success("退款已通过");
    }

    @PostMapping("/refund/reject")
    public Result<String> rejectRefund(@RequestParam String orderNo) {
        TicketOrder order = ticketOrderService.findByOrderNo(orderNo);
        if (order == null) return Result.error("订单不存在");
        order.setRefundStatus("refund_rejected");
        ticketOrderService.update(order);
        return Result.success("退款已拒绝");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = ticketOrderService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(ticketOrderService.getStats());
    }
}
