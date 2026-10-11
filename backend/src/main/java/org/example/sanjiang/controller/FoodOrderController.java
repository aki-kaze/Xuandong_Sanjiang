package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.FoodOrder;
import org.example.sanjiang.service.FoodOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/food-orders")
@CrossOrigin
public class FoodOrderController {

    @Autowired
    private FoodOrderService foodOrderService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String searchKey,
            @RequestParam(required = false) Long userId) {
        List<FoodOrder> list;
        int total;
        if (userId != null) {
            list = foodOrderService.findByUserId(userId, page, size);
            total = foodOrderService.countByUserId(userId);
        } else {
            list = foodOrderService.findAll(page, size, status, searchKey);
            total = foodOrderService.count(status, searchKey);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/user/{userId}")
    public Result<List<FoodOrder>> getByUserId(@PathVariable Long userId) {
        return Result.success(foodOrderService.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public Result<FoodOrder> getById(@PathVariable Long id) {
        FoodOrder order = foodOrderService.findById(id);
        return Result.success(order);
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> data) {
        FoodOrder order = new FoodOrder();
        if (data.get("orderNo") != null) order.setOrderNo(data.get("orderNo").toString());
        order.setUserId(data.get("userId") != null ? Long.parseLong(data.get("userId").toString()) : 0L);
        order.setFoodId(data.get("foodId") != null ? Long.parseLong(data.get("foodId").toString()) : 0L);
        order.setFoodName(data.getOrDefault("foodName", "").toString());
        order.setFoodCover(data.getOrDefault("foodCover", "").toString());
        order.setShopName(data.getOrDefault("shopName", "").toString());
        order.setQuantity(data.get("quantity") != null ? Integer.parseInt(data.get("quantity").toString()) : 1);
        if (data.get("price") != null) order.setPrice(new java.math.BigDecimal(data.get("price").toString()));
        if (data.get("totalPrice") != null) order.setTotalPrice(new java.math.BigDecimal(data.get("totalPrice").toString()));
        order.setContactName(data.getOrDefault("contactName", "").toString());
        order.setContactPhone(data.getOrDefault("contactPhone", "").toString());
        order.setRemark(data.getOrDefault("remark", "").toString());
        Map<String, Object> result = foodOrderService.create(order);
        return Result.success(result);
    }

    @PostMapping("/pay")
    public Result<String> pay(@RequestParam String orderNo) {
        boolean success = foodOrderService.pay(orderNo);
        return success ? Result.success("支付成功") : Result.error("支付失败");
    }

    @PostMapping("/cancel")
    public Result<String> cancel(@RequestParam String orderNo) {
        boolean success = foodOrderService.cancel(orderNo);
        return success ? Result.success("取消成功") : Result.error("取消失败");
    }

    @PostMapping("/use")
    public Result<String> use(@RequestParam String orderNo) {
        boolean success = foodOrderService.use(orderNo);
        return success ? Result.success("核销成功") : Result.error("核销失败");
    }

    @PostMapping("/complete")
    public Result<String> complete(@RequestParam String orderNo) {
        boolean success = foodOrderService.complete(orderNo);
        return success ? Result.success("完成成功") : Result.error("完成失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = foodOrderService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(foodOrderService.getStats());
    }
}
