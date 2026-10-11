package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.SensitiveWord;
import org.example.sanjiang.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sensitive-words")
@CrossOrigin
public class SensitiveWordController {

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @GetMapping
    public Result<List<SensitiveWord>> getAll() {
        return Result.success(sensitiveWordService.findAll());
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayHits", sensitiveWordService.getTodayHitCount());
        stats.put("totalHits", sensitiveWordService.getTotalHitCount());
        return Result.success(stats);
    }

    @PostMapping
    public Result<String> save(@RequestBody SensitiveWord word) {
        boolean success = sensitiveWordService.save(word);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody SensitiveWord word) {
        boolean success = sensitiveWordService.update(word);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = sensitiveWordService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PostMapping("/check")
    public Result<Map<String, Object>> check(@RequestBody Map<String, String> body) {
        String text = body.getOrDefault("text", "");
        List<String> found = sensitiveWordService.checkText(text);
        // 命中敏感词时更新命中次数
        for (String word : found) {
            sensitiveWordService.incrementHitCount(word);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("hasSensitive", !found.isEmpty());
        result.put("words", found);
        return Result.success(result);
    }
}
