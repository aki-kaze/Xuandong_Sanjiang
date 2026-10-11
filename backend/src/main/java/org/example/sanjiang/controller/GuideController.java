package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.Guide;
import org.example.sanjiang.service.GuideService;
import org.example.sanjiang.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/guides")
@CrossOrigin
public class GuideController {

    @Autowired
    private GuideService guideService;
    
    @Autowired(required = false)
    private SensitiveWordService sensitiveWordService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) String keyword) {
        List<Guide> list;
        int total;
        if (keyword != null && !keyword.isEmpty()) {
            list = guideService.searchByTitle(keyword, page, size);
            total = guideService.countByTitle(keyword);
        } else if (auditStatus != null) {
            list = guideService.findByAuditStatus(auditStatus, page, size);
            total = guideService.countByAuditStatus(auditStatus);
        } else if (status != null) {
            list = guideService.findByStatus(status, page, size);
            total = guideService.count();
        } else {
            list = guideService.findAll(page, size);
            total = guideService.count();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Guide> getById(@PathVariable Long id) {
        Guide guide = guideService.findById(id);
        return Result.success(guide);
    }

    @PostMapping
    public Result<String> save(@RequestBody Guide guide) {
        // 敏感词检测
        if (sensitiveWordService != null) {
            String content = guide.getTitle() + " " + (guide.getDesc() != null ? guide.getDesc() : "");
            if (content != null && !content.trim().isEmpty()) {
                List<String> sensitiveWords = sensitiveWordService.checkText(content);
                if (!sensitiveWords.isEmpty()) {
                    return Result.error("内容包含敏感词：" + String.join("、", sensitiveWords));
                }
            }
        }
        
        if (guide.getPublishTime() == null) {
            guide.setPublishTime(LocalDateTime.now());
        }
        if (guide.getStatus() == null) {
            guide.setStatus(0); // 新攻略默认待审核
        }
        boolean success = guideService.save(guide);
        return success ? Result.success("保存成功，待审核") : Result.error("保存失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Guide guide) {
        boolean success = guideService.update(guide);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = guideService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/count/pending")
    public Result<Integer> countPending() {
        return Result.success(guideService.countByAuditStatus(0));
    }
}
