package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.Admin;
import org.example.sanjiang.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private AdminService adminService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        if (username == null || username.trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        Admin admin = adminService.login(username, password);
        if (admin == null) {
            return Result.error("用户名或密码错误");
        }

        // 生成 token（这里简化处理，实际应该使用 JWT）
        String token = "token_" + admin.getId() + "_" + System.currentTimeMillis();

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", admin);

        return Result.success(result);
    }

    /**
     * 管理员注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody Admin admin) {
        if (admin.getUsername() == null || admin.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (admin.getPassword() == null || admin.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        // 检查用户名是否已存在
        Admin existAdmin = adminService.findByUsername(admin.getUsername());
        if (existAdmin != null) {
            return Result.error("用户名已存在");
        }

        // 设置默认值
        if (admin.getNickname() == null || admin.getNickname().trim().isEmpty()) {
            admin.setNickname(admin.getUsername());
        }
        if (admin.getRole() == null || admin.getRole().trim().isEmpty()) {
            admin.setRole("admin");
        }
        admin.setStatus(1);

        boolean success = adminService.save(admin);
        return success ? Result.success("注册成功") : Result.error("注册失败");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<Admin> getInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return Result.error(401, "未登录");
        }

        // 简化处理：从 token 中解析用户 ID
        // 支持 "Bearer token_xxx_xxx" 和 "token_xxx_xxx" 两种格式
        try {
            String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            String[] parts = rawToken.replace("token_", "").split("_");
            Integer adminId = Integer.parseInt(parts[0]);
            Admin admin = adminService.findById(adminId);
            if (admin != null) {
                // 不返回密码
                admin.setPassword(null);
                return Result.success(admin);
            }
        } catch (Exception e) {
            // ignore
        }

        return Result.error(401, "登录已过期");
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("退出成功");
    }
}
