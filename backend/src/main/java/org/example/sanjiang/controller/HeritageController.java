package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.Heritage;
import org.example.sanjiang.service.HeritageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/heritage")
@CrossOrigin
public class HeritageController {

    @Autowired
    private HeritageService heritageService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean admin) {
        List<Heritage> list;
        int total;
        if (admin) {
            if (level != null && !level.isEmpty() && keyword != null && !keyword.isEmpty()) {
                list = heritageService.findAll(page, size, level, keyword);
                total = heritageService.count(level, keyword);
            } else if (level != null && !level.isEmpty()) {
                list = heritageService.findAll(page, size, level, null);
                total = heritageService.count(level, null);
            } else if (keyword != null && !keyword.isEmpty()) {
                list = heritageService.findAll(page, size, null, keyword);
                total = heritageService.count(null, keyword);
            } else {
                list = heritageService.findAllAdmin(page, size);
                total = heritageService.countAdmin();
            }
        } else {
            list = heritageService.findAll(page, size, level, keyword);
            total = heritageService.count(level, keyword);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Heritage> getById(@PathVariable Integer id) {
        Heritage heritage = heritageService.findById(id);
        return Result.success(heritage);
    }

    @PostMapping
    public Result<String> save(@RequestBody Heritage heritage) {
        boolean success = heritageService.save(heritage);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Heritage heritage) {
        boolean success = heritageService.update(heritage);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        boolean success = heritageService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
