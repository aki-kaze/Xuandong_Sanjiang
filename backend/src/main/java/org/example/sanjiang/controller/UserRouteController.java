package org.example.sanjiang.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.sanjiang.entity.UserRoute;
import org.example.sanjiang.service.UserRouteService;
import org.example.sanjiang.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-routes")
@CrossOrigin
public class UserRouteController {

    @Autowired
    private UserRouteService userRouteService;

    @GetMapping
    public Result<PageInfo<UserRoute>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String status) {
        List<UserRoute> list;

        // 如果有筛选条件，使用条件查询
        if ((keyword != null && !keyword.isEmpty()) || userId != null || (status != null && !status.isEmpty())) {
            list = userRouteService.findByConditions(page, size, keyword, userId, status);
        } else {
            list = userRouteService.findAll(page, size);
        }

        // 为每条路线提取封面
        for (UserRoute route : list) {
            if (route.getCover() == null || route.getCover().isEmpty()) {
                route.setCover(extractCoverFromDaily(route.getDaily()));
            }
        }
        PageInfo<UserRoute> pageInfo = new PageInfo<>(list);
        return Result.success(pageInfo);
    }

    @GetMapping("/my")
    public Result<List<UserRoute>> getMyRoutes(@RequestParam Integer userId) {
        List<UserRoute> routes = userRouteService.findByUserId(userId);
        // 为每条路线提取封面
        for (UserRoute route : routes) {
            if (route.getCover() == null || route.getCover().isEmpty()) {
                route.setCover(extractCoverFromDaily(route.getDaily()));
            }
        }
        return Result.success(routes);
    }

    @GetMapping("/{id}")
    public Result<UserRoute> get(@PathVariable Integer id) {
        UserRoute route = userRouteService.findById(id);
        if (route != null && (route.getCover() == null || route.getCover().isEmpty())) {
            route.setCover(extractCoverFromDaily(route.getDaily()));
        }
        return Result.success(route);
    }

    @PostMapping
    public Result<Map<String, Object>> save(@RequestBody UserRoute userRoute) {
        // 敏感词检测
        if (userRoute.getDescription() != null && !userRoute.getDescription().isEmpty()) {
            List<String> sensitiveWords = userRouteService.checkRouteDescription(userRoute.getDescription());
            if (!sensitiveWords.isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("errorCode", "SENSITIVE_WORDS");
                errorResult.put("message", "描述包含敏感词：" + String.join("、", sensitiveWords));
                errorResult.put("sensitiveWords", sensitiveWords);
                // 正确写法：用 success 包装错误数据（后端通用做法）
                return Result.success(errorResult);
            }
        }
        
        // 自动提取封面
        if (userRoute.getCover() == null || userRoute.getCover().isEmpty()) {
            userRoute.setCover(extractCoverFromDaily(userRoute.getDaily()));
        }
        boolean success = userRouteService.save(userRoute);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("id", userRoute.getId());
        return success ? Result.success(result) : Result.error("保存失败");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody UserRoute userRoute) {
        userRoute.setId(id);
        // 敏感词检测
        if (userRoute.getDescription() != null && !userRoute.getDescription().isEmpty()) {
            List<String> sensitiveWords = userRouteService.checkRouteDescription(userRoute.getDescription());
            if (!sensitiveWords.isEmpty()) {
                return Result.error("描述包含敏感词：" + String.join("、", sensitiveWords));
            }
        }
        // 自动提取封面
        if (userRoute.getCover() == null || userRoute.getCover().isEmpty()) {
            userRoute.setCover(extractCoverFromDaily(userRoute.getDaily()));
        }
        boolean success = userRouteService.update(userRoute);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        boolean success = userRouteService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    /**
     * 从 daily JSON 中提取第一个景点的封面
     */
    private String extractCoverFromDaily(String dailyJson) {
        if (dailyJson == null || dailyJson.isEmpty()) {
            return null;
        }
        try {
            // 简单的 JSON 解析，提取第一个 cover 字段
            // 格式：[{"stops":[{"cover":"xxx"},...]},...]
            int coverStart = dailyJson.indexOf("\"cover\"");
            if (coverStart > 0) {
                int colonPos = dailyJson.indexOf(":", coverStart);
                if (colonPos > 0) {
                    int quoteStart = dailyJson.indexOf("\"", colonPos + 1);
                    if (quoteStart > 0) {
                        int quoteEnd = dailyJson.indexOf("\"", quoteStart + 1);
                        if (quoteEnd > 0) {
                            return dailyJson.substring(quoteStart + 1, quoteEnd);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 解析失败，返回 null
        }
        return null;
    }
}
