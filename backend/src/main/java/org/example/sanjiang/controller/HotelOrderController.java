package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.HotelOrder;
import org.example.sanjiang.service.HotelOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotel-orders")
@CrossOrigin
public class HotelOrderController {

    @Autowired
    private HotelOrderService hotelOrderService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String searchKey,
            @RequestParam(required = false) Long userId) {
        List<HotelOrder> list;
        int total;
        if (userId != null) {
            list = hotelOrderService.findByUserId(userId, page, size);
            total = hotelOrderService.countByUserId(userId);
        } else {
            list = hotelOrderService.findAll(page, size, status, searchKey);
            total = hotelOrderService.count(status, searchKey);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/user/{userId}")
    public Result<List<HotelOrder>> getByUserId(@PathVariable String userId) {
        try {
            Long id = Long.parseLong(userId);
            return Result.success(hotelOrderService.findByUserId(id));
        } catch (NumberFormatException e) {
            return Result.success(java.util.Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public Result<HotelOrder> getById(@PathVariable Long id) {
        HotelOrder order = hotelOrderService.findById(id);
        return Result.success(order);
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> data) {
        HotelOrder order = new HotelOrder();
        if (data.get("orderNo") != null) order.setOrderNo(data.get("orderNo").toString());
        try {
            order.setUserId(data.get("userId") != null ? Long.parseLong(data.get("userId").toString()) : 0L);
        } catch (NumberFormatException e) {
            order.setUserId(0L);
        }
        try {
            order.setHotelId(data.get("hotelId") != null ? Long.parseLong(data.get("hotelId").toString()) : 0L);
        } catch (NumberFormatException e) {
            order.setHotelId(0L);
        }
        order.setHotelName(data.getOrDefault("hotelName", "").toString());
        order.setHotelCover(data.getOrDefault("hotelCover", "").toString());
        order.setRoomType(data.getOrDefault("roomType", "").toString());
        try {
            order.setQuantity(data.get("quantity") != null ? Integer.parseInt(data.get("quantity").toString()) : 1);
        } catch (NumberFormatException e) {
            order.setQuantity(1);
        }
        if (data.get("price") != null) order.setPrice(new java.math.BigDecimal(data.get("price").toString()));
        if (data.get("totalPrice") != null) order.setTotalPrice(new java.math.BigDecimal(data.get("totalPrice").toString()));
        if (data.get("checkInDate") != null) order.setCheckInDate(data.get("checkInDate").toString());
        if (data.get("checkOutDate") != null) order.setCheckOutDate(data.get("checkOutDate").toString());
        order.setContactName(data.getOrDefault("contactName", "").toString());
        order.setContactPhone(data.getOrDefault("contactPhone", "").toString());
        order.setRemark(data.getOrDefault("remark", "").toString());
        Map<String, Object> result = hotelOrderService.create(order);
        return Result.success(result);
    }

    @PostMapping("/pay")
    public Result<String> pay(@RequestParam String orderNo) {
        boolean success = hotelOrderService.pay(orderNo);
        return success ? Result.success("支付成功") : Result.error("支付失败");
    }

    @PostMapping("/cancel")
    public Result<String> cancel(@RequestParam String orderNo) {
        boolean success = hotelOrderService.cancel(orderNo);
        return success ? Result.success("取消成功") : Result.error("取消失败");
    }

    @PostMapping("/use")
    public Result<String> use(@RequestParam String orderNo) {
        boolean success = hotelOrderService.use(orderNo);
        return success ? Result.success("核销成功") : Result.error("核销失败");
    }

    @PostMapping("/check-in")
    public Result<String> checkIn(@RequestParam String orderNo) {
        boolean success = hotelOrderService.checkIn(orderNo);
        return success ? Result.success("入住成功") : Result.error("入住失败");
    }

    @PostMapping("/check-out")
    public Result<String> checkOut(@RequestParam String orderNo) {
        boolean success = hotelOrderService.checkOut(orderNo);
        return success ? Result.success("退房成功") : Result.error("退房失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = hotelOrderService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(hotelOrderService.getStats());
    }
}
