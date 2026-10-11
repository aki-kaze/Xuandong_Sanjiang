package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.Hotel;
import org.example.sanjiang.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotels")
@CrossOrigin
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean admin) {
        List<Hotel> list;
        int total;
        if (admin) {
            // 后台管理模式下也支持类型和关键词筛选
            if (type != null && !type.isEmpty() && keyword != null && !keyword.isEmpty()) {
                list = hotelService.findAll(page, size, type, keyword);
                total = hotelService.count(type, keyword);
            } else if (type != null && !type.isEmpty()) {
                list = hotelService.findAll(page, size, type, null);
                total = hotelService.count(type, null);
            } else if (keyword != null && !keyword.isEmpty()) {
                list = hotelService.findAll(page, size, null, keyword);
                total = hotelService.count(null, keyword);
            } else {
                list = hotelService.findAllAdmin(page, size);
                total = hotelService.countAdmin();
            }
        } else {
            list = hotelService.findAll(page, size, type, keyword);
            total = hotelService.count(type, keyword);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Hotel> getById(@PathVariable Long id) {
        Hotel hotel = hotelService.findById(id.intValue());
        return Result.success(hotel);
    }

    @PostMapping
    public Result<String> save(@RequestBody Hotel hotel) {
        boolean success = hotelService.save(hotel);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Hotel hotel) {
        boolean success = hotelService.update(hotel);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = hotelService.deleteById(id.intValue());
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
