package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.ExperienceOrder;
import org.example.sanjiang.service.ExperienceOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/experience-orders")
@CrossOrigin
public class ExperienceOrderController {

    @Autowired
    private ExperienceOrderService experienceOrderService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String searchKey,
            @RequestParam(required = false) Long userId) {
        List<ExperienceOrder> list;
        int total;
        if (userId != null) {
            list = experienceOrderService.findByUserId(userId, page, size);
            total = experienceOrderService.countByUserId(userId);
        } else {
            list = experienceOrderService.findByCondition(status, searchKey, page, size);
            total = experienceOrderService.countByCondition(status, searchKey);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/user/{userId}")
    public Result<List<ExperienceOrder>> getByUserId(@PathVariable Long userId) {
        return Result.success(experienceOrderService.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public Result<ExperienceOrder> getById(@PathVariable Long id) {
        ExperienceOrder order = experienceOrderService.findById(id);
        return Result.success(order);
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> data) {
        ExperienceOrder order = new ExperienceOrder();
        if (data.get("orderNo") != null) order.setOrderNo(data.get("orderNo").toString());
        order.setUserId(data.get("userId") != null ? Long.parseLong(data.get("userId").toString()) : 0L);
        order.setHeritageId(data.get("heritageId") != null ? Integer.parseInt(data.get("heritageId").toString()) : 0);
        order.setHeritageName(data.getOrDefault("heritageName", "").toString());
        order.setHeritageCover(data.getOrDefault("heritageCover", "").toString());
        order.setExperienceType(data.getOrDefault("experienceType", "").toString());
        order.setQuantity(data.get("quantity") != null ? Integer.parseInt(data.get("quantity").toString()) : 1);
        if (data.get("price") != null) order.setPrice(new java.math.BigDecimal(data.get("price").toString()));
        if (data.get("totalPrice") != null) order.setTotalPrice(new java.math.BigDecimal(data.get("totalPrice").toString()));
        order.setContactName(data.getOrDefault("contactName", "").toString());
        order.setContactPhone(data.getOrDefault("contactPhone", "").toString());
        if (data.get("visitDate") != null) order.setVisitDate(data.get("visitDate").toString());
        order.setVisitTime(data.getOrDefault("visitTime", "").toString());
        order.setRemark(data.getOrDefault("remark", "").toString());
        Map<String, Object> result = experienceOrderService.create(order);
        return Result.success(result);
    }

    @PostMapping("/confirm")
    public Result<String> confirm(@RequestParam String orderNo) {
        boolean success = experienceOrderService.confirm(orderNo);
        return success ? Result.success("确认成功") : Result.error("确认失败");
    }

    @PostMapping("/complete")
    public Result<String> complete(@RequestParam String orderNo) {
        boolean success = experienceOrderService.complete(orderNo);
        return success ? Result.success("完成成功") : Result.error("完成失败");
    }

    @PostMapping("/cancel")
    public Result<String> cancel(@RequestParam String orderNo,
                                  @RequestParam(required = false) String reason) {
        boolean success = experienceOrderService.cancel(orderNo, reason);
        return success ? Result.success("取消成功") : Result.error("取消失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = experienceOrderService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(experienceOrderService.getStats());
    }
}
