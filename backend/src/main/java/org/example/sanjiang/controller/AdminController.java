package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.Admin;
import org.example.sanjiang.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String password = data.get("password");

        Admin admin = adminService.login(username, password);

        if (admin != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("token", "admin_token_" + System.currentTimeMillis());
            result.put("userInfo", admin);
            return Result.success(result);
        } else {
            return Result.error("用户名或密码错误");
        }
    }

    @GetMapping("/info")
    public Result<Admin> getInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return Result.error(401, "未登录");
        }

        // 简化处理：返回默认管理员信息
        // 实际应该从 token 中解析用户 ID
        Admin admin = adminService.findById(1);
        if (admin != null) {
            admin.setPassword(null); // 不返回密码
            return Result.success(admin);
        }

        return Result.error(401, "登录已过期");
    }
}
