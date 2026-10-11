package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.Attraction;
import org.example.sanjiang.service.AttractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attractions")
@CrossOrigin
public class AttractionController {

    @Autowired
    private AttractionService attractionService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean admin) {
        List<Attraction> list;
        int total;
        if (admin) {
            // 后台管理模式下也支持分类和关键词筛选
            if (category != null && !category.isEmpty() && keyword != null && !keyword.isEmpty()) {
                list = attractionService.findAll(page, size, category, keyword);
                total = attractionService.count(category, keyword);
            } else if (category != null && !category.isEmpty()) {
                list = attractionService.findAll(page, size, category, null);
                total = attractionService.count(category, null);
            } else if (keyword != null && !keyword.isEmpty()) {
                list = attractionService.findAll(page, size, null, keyword);
                total = attractionService.count(null, keyword);
            } else {
                list = attractionService.findAllAdmin(page, size);
                total = attractionService.countAdmin();
            }
        } else {
            list = attractionService.findAll(page, size, category, keyword);
            total = attractionService.count(category, keyword);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Attraction> getById(@PathVariable Long id) {
        Attraction attraction = attractionService.findById(id.intValue());
        return Result.success(attraction);
    }

    @GetMapping("/search")
    public Result<List<Attraction>> search(@RequestParam String keyword) {
        List<Attraction> list = attractionService.search(keyword);
        return Result.success(list);
    }

    @PostMapping
    public Result<String> save(@RequestBody Attraction attraction) {
        boolean success = attractionService.save(attraction);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Attraction attraction) {
        boolean success = attractionService.update(attraction);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        boolean success = attractionService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/count")
    public Result<Integer> count() {
        return Result.success(attractionService.count());
    }
}
