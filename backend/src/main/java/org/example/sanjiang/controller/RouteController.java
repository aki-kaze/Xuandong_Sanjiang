package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.Route;
import org.example.sanjiang.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin
public class RouteController {

    @Autowired
    private RouteService routeService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean admin) {
        List<Route> list;
        int total;
        if (admin) {
            if (difficulty != null && !difficulty.isEmpty() && keyword != null && !keyword.isEmpty()) {
                list = routeService.findAll(page, size, difficulty, keyword);
                total = routeService.count(difficulty, keyword);
            } else if (difficulty != null && !difficulty.isEmpty()) {
                list = routeService.findAll(page, size, difficulty, null);
                total = routeService.count(difficulty, null);
            } else if (keyword != null && !keyword.isEmpty()) {
                list = routeService.findAll(page, size, null, keyword);
                total = routeService.count(null, keyword);
            } else {
                list = routeService.findAllAdmin(page, size);
                total = routeService.countAdmin();
            }
        } else {
            list = routeService.findAll(page, size, difficulty, keyword);
            total = routeService.count(difficulty, keyword);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Route> getById(@PathVariable Long id) {
        Route route = routeService.findById(id.intValue());
        return Result.success(route);
    }

    @PostMapping
    public Result<Map<String, Object>> save(@RequestBody Route route) {
        boolean success = routeService.save(route);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("id", route.getId());
        return success ? Result.success(result) : Result.error("保存失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Route route) {
        boolean success = routeService.update(route);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = routeService.deleteById(id.intValue());
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
