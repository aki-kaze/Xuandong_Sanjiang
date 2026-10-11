package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.Order;
import org.example.sanjiang.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId) {
        List<Order> list;
        int total;
        if (userId != null) {
            list = orderService.findByUserId(userId, page, size);
            total = orderService.countByUserId(userId);
        } else {
            list = orderService.findAll(page, size);
            total = orderService.count();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Order> getById(@PathVariable Long id) {
        Order order = orderService.findById(id);
        return Result.success(order);
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> data) {
        Order order = new Order();
        order.setUserId(data.get("userId") != null ? Long.parseLong(data.get("userId").toString()) : 0L);
        order.setType(data.getOrDefault("remark", "ticket").toString().contains("美食") ? "food" : "ticket");
        order.setItemId(data.get("ticketId") != null ? Long.parseLong(data.get("ticketId").toString()) : 0L);
        order.setItemName(data.getOrDefault("ticketName", "").toString());
        order.setQuantity(data.get("quantity") != null ? Integer.parseInt(data.get("quantity").toString()) : 1);
        if (data.get("price") != null) order.setPrice(new java.math.BigDecimal(data.get("price").toString()));
        if (data.get("totalAmount") != null) order.setTotalPrice(new java.math.BigDecimal(data.get("totalAmount").toString()));
        order.setContactName(data.getOrDefault("contactName", "").toString());
        order.setContactPhone(data.getOrDefault("contactPhone", "").toString());
        if (data.get("visitDate") != null) order.setVisitDate(data.get("visitDate").toString());
        order.setRemark(data.getOrDefault("remark", "").toString());
        Map<String, Object> result = orderService.create(order);
        return Result.success(result);
    }

    @PostMapping("/pay")
    public Result<String> pay(@RequestParam String orderNo) {
        boolean success = orderService.pay(orderNo);
        return success ? Result.success("支付成功") : Result.error("支付失败");
    }

    @PostMapping("/cancel")
    public Result<String> cancel(@RequestParam String orderNo) {
        boolean success = orderService.cancel(orderNo);
        return success ? Result.success("取消成功") : Result.error("取消失败");
    }

    @PostMapping("/use")
    public Result<String> use(@RequestParam String orderNo) {
        boolean success = orderService.use(orderNo);
        return success ? Result.success("核销成功") : Result.error("核销失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = orderService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(orderService.getStats());
    }
}
