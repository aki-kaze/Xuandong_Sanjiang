package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.Food;
import org.example.sanjiang.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/foods")
@CrossOrigin
public class FoodController {

    @Autowired
    private FoodService foodService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean admin) {
        List<Food> list;
        int total;
        if (admin) {
            if (category != null && !category.isEmpty() && keyword != null && !keyword.isEmpty()) {
                list = foodService.findAll(page, size, category, keyword);
                total = foodService.count(category, keyword);
            } else if (category != null && !category.isEmpty()) {
                list = foodService.findAll(page, size, category, null);
                total = foodService.count(category, null);
            } else if (keyword != null && !keyword.isEmpty()) {
                list = foodService.findAll(page, size, null, keyword);
                total = foodService.count(null, keyword);
            } else {
                list = foodService.findAllAdmin(page, size);
                total = foodService.countAdmin();
            }
        } else {
            list = foodService.findAll(page, size, category, keyword);
            total = foodService.count(category, keyword);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Food> getById(@PathVariable Long id) {
        Food food = foodService.findById(id.intValue());
        return Result.success(food);
    }

    @PostMapping
    public Result<String> save(@RequestBody Food food) {
        boolean success = foodService.save(food);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Food food) {
        boolean success = foodService.update(food);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = foodService.deleteById(id.intValue());
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
