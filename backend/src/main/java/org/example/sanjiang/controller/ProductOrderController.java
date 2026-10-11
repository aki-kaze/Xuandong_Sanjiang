package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.ProductOrder;
import org.example.sanjiang.service.ProductOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product-orders")
@CrossOrigin
public class ProductOrderController {

    @Autowired
    private ProductOrderService productOrderService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String searchKey,
            @RequestParam(required = false) Long userId) {
        List<ProductOrder> list;
        int total;
        if (userId != null) {
            list = productOrderService.findByUserId(userId, page, size);
            total = productOrderService.countByUserId(userId);
        } else {
            list = productOrderService.findAll(page, size, status, searchKey);
            total = productOrderService.count(status, searchKey);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/user/{userId}")
    public Result<List<ProductOrder>> getByUserId(@PathVariable Long userId) {
        return Result.success(productOrderService.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public Result<ProductOrder> getById(@PathVariable Long id) {
        ProductOrder order = productOrderService.findById(id);
        return Result.success(order);
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> data) {
        ProductOrder order = new ProductOrder();
        if (data.get("orderNo") != null) order.setOrderNo(data.get("orderNo").toString());
        order.setUserId(data.get("userId") != null ? Long.parseLong(data.get("userId").toString()) : 0L);
        order.setProductId(data.get("productId") != null ? Long.parseLong(data.get("productId").toString()) : 0L);
        order.setProductName(data.getOrDefault("productName", "").toString());
        order.setProductCover(data.getOrDefault("productCover", "").toString());
        order.setQuantity(data.get("quantity") != null ? Integer.parseInt(data.get("quantity").toString()) : 1);
        if (data.get("price") != null) order.setPrice(new java.math.BigDecimal(data.get("price").toString()));
        if (data.get("totalPrice") != null) order.setTotalPrice(new java.math.BigDecimal(data.get("totalPrice").toString()));
        order.setContactName(data.getOrDefault("contactName", "").toString());
        order.setContactPhone(data.getOrDefault("contactPhone", "").toString());
        order.setAddress(data.getOrDefault("address", "").toString());
        order.setRemark(data.getOrDefault("remark", "").toString());
        Map<String, Object> result = productOrderService.create(order);
        return Result.success(result);
    }

    @PostMapping("/pay")
    public Result<String> pay(@RequestParam String orderNo) {
        boolean success = productOrderService.pay(orderNo);
        return success ? Result.success("支付成功") : Result.error("支付失败");
    }

    @PostMapping("/cancel")
    public Result<String> cancel(@RequestParam String orderNo) {
        boolean success = productOrderService.cancel(orderNo);
        return success ? Result.success("取消成功") : Result.error("取消失败");
    }

    @PostMapping("/use")
    public Result<String> use(@RequestParam String orderNo) {
        boolean success = productOrderService.use(orderNo);
        return success ? Result.success("核销成功") : Result.error("核销失败");
    }

    @PostMapping("/ship")
    public Result<String> ship(@RequestParam String orderNo) {
        boolean success = productOrderService.ship(orderNo);
        return success ? Result.success("发货成功") : Result.error("发货失败");
    }

    @PostMapping("/deliver")
    public Result<String> deliver(@RequestParam String orderNo) {
        boolean success = productOrderService.deliver(orderNo);
        return success ? Result.success("收货成功") : Result.error("收货失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = productOrderService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(productOrderService.getStats());
    }
}
