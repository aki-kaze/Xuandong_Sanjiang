package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.Activity;
import org.example.sanjiang.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        List<Activity> list;
        if (status != null && !status.isEmpty()) {
            list = activityService.findByStatus(status, page, size);
        } else {
            list = activityService.findAll(page, size);
        }
        int total = activityService.count();

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Activity> getById(@PathVariable Long id) {
        Activity activity = activityService.findById(id);
        return Result.success(activity);
    }

    @PostMapping
    public Result<String> save(@RequestBody Activity activity) {
        boolean success = activityService.save(activity);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Activity activity) {
        boolean success = activityService.update(activity);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = activityService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PostMapping("/signup/{id}")
    public Result<String> signup(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        Activity activity = activityService.findById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        // 检查活动状态，已结束的活动无法报名
        if ("已结束".equals(activity.getStatus())) {
            return Result.error("活动已结束，无法报名");
        }
        if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            return Result.error("活动已满员");
        }
        activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
        boolean success = activityService.update(activity);
        return success ? Result.success("报名成功") : Result.error("报名失败");
    }

    @PostMapping("/signup/{id}/cancel")
    public Result<String> cancelSignup(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        Activity activity = activityService.findById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        if (activity.getCurrentParticipants() > 0) {
            activity.setCurrentParticipants(activity.getCurrentParticipants() - 1);
            boolean success = activityService.update(activity);
            return success ? Result.success("取消成功") : Result.error("取消失败");
        }
        return Result.error("当前报名人数为 0，无法取消");
    }

    @PostMapping("/publish/{id}")
    public Result<String> publish(@PathVariable Long id) {
        Activity activity = activityService.findById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        boolean success = activityService.publish(id);
        return success ? Result.success("上架成功") : Result.error("上架失败");
    }

    @PostMapping("/unpublish/{id}")
    public Result<String> unpublish(@PathVariable Long id) {
        Activity activity = activityService.findById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        boolean success = activityService.unpublish(id);
        return success ? Result.success("下架成功") : Result.error("下架失败");
    }

    @GetMapping("/published")
    public Result<Map<String, Object>> getPublished(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Activity> list = activityService.findPublished(page, size);
        int total = activityService.count();

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }
}
