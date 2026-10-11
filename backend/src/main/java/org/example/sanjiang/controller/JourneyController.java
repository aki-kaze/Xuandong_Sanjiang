package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.Journey;
import org.example.sanjiang.service.JourneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/journeys")
@CrossOrigin
public class JourneyController {

    @Autowired
    private JourneyService journeyService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Journey> list = journeyService.findAll(page, size);
        int total = journeyService.count();

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/user/{userId}")
    public Result<Map<String, Object>> getByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Journey> list = journeyService.findByUserId(userId, page, size);
        int total = journeyService.countByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Journey> getById(@PathVariable Long id) {
        Journey journey = journeyService.findById(id);
        return Result.success(journey);
    }

    @PostMapping
    public Result<String> save(@RequestBody Journey journey) {
        boolean success = journeyService.save(journey);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Journey journey) {
        boolean success = journeyService.update(journey);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = journeyService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
