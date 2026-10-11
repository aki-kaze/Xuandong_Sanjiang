package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.AiKnowledge;
import org.example.sanjiang.service.AiKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-knowledge")
@CrossOrigin
public class AiKnowledgeController {

    @Autowired
    private AiKnowledgeService aiKnowledgeService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        List<AiKnowledge> list = aiKnowledgeService.findBySearch(page, size, keyword, category);
        int total = aiKnowledgeService.count(keyword, category);

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<AiKnowledge> getById(@PathVariable Long id) {
        return Result.success(aiKnowledgeService.findById(id));
    }

    @PostMapping
    public Result<String> save(@RequestBody AiKnowledge knowledge) {
        boolean success = aiKnowledgeService.save(knowledge);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody AiKnowledge knowledge) {
        boolean success = aiKnowledgeService.update(knowledge);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = aiKnowledgeService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PutMapping("/batch-enabled")
    public Result<String> batchUpdateEnabled(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) params.get("ids");
        Integer enabled = (Integer) params.get("enabled");
        boolean success = aiKnowledgeService.batchUpdateEnabled(ids, enabled);
        return success ? Result.success("操作成功") : Result.error("操作失败");
    }

    @DeleteMapping("/batch")
    public Result<String> batchDelete(@RequestBody List<Long> ids) {
        boolean success = aiKnowledgeService.batchDeleteByIds(ids);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
