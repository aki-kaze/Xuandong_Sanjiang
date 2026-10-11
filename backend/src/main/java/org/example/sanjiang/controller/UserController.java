package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.User;
import org.example.sanjiang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        List<User> list;
        int total;
        if (keyword != null && !keyword.trim().isEmpty()) {
            list = userService.search(keyword, page, size);
            total = userService.countSearch(keyword);
        } else {
            list = userService.findAll(page, size);
            total = userService.count();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.findById(id.intValue());
        return Result.success(user);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, Object> data) {
        String phone = (String) data.get("phone");
        String password = (String) data.get("password");

        if (phone == null || phone.trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        User user = userService.findByPhone(phone);
        if (user == null || !password.equals(user.getPassword())) {
            return Result.error("手机号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        String token = "user_" + user.getId() + "_" + System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("id", user.getId());
        result.put("nickname", user.getNickname());
        result.put("name", user.getName());
        result.put("avatar", user.getAvatar());
        result.put("phone", user.getPhone());
        return Result.success(result);
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, Object> data) {
        String phone = (String) data.get("phone");
        String password = (String) data.get("password");
        String nickname = (String) data.get("nickname");

        if (phone == null || phone.trim().isEmpty()) return Result.error("手机号不能为空");
        if (password == null || password.trim().isEmpty()) return Result.error("密码不能为空");

        User existing = userService.findByPhone(phone);
        if (existing != null) return Result.error("该手机号已注册");

        User user = new User();
        user.setPhone(phone);
        user.setPassword(password);
        user.setNickname(nickname != null ? nickname : "侗乡旅行者");
        user.setName(data.get("name") != null ? data.get("name").toString() : null);
        user.setStatus(1);
        user.setGender(0);
        // 注册时随机分配头像
        user.setAvatar(userService.getRandomAvatar());
        userService.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("nickname", user.getNickname());
        result.put("name", user.getName());
        result.put("avatar", user.getAvatar());
        result.put("phone", user.getPhone());
        return Result.success(result);
    }

    /**
     * 游客登录 - 生成游客token
     * 游客只能查看信息，不能进行写操作
     */
    @PostMapping("/guest")
    public Result<Map<String, Object>> guestLogin() {
        String guestToken = "guest_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);

        Map<String, Object> result = new HashMap<>();
        result.put("token", guestToken);
        result.put("isGuest", true);
        result.put("nickname", "游客");
        result.put("message", "游客模式：只能查看信息，登录后可使用完整功能");
        return Result.success(result);
    }

    @GetMapping("/me")
    public Result<User> getMe(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null) return Result.error(401, "未登录");
        try {
            String raw = token.startsWith("Bearer ") ? token.substring(7) : token;

            // 处理游客token
            if (raw.startsWith("guest_")) {
                // 返回游客信息
                User guestUser = new User();
                guestUser.setNickname("游客");
                guestUser.setAvatar("");
                guestUser.setPhone("");
                return Result.success(guestUser);
            }

            // 处理用户token
            if (raw.startsWith("user_")) {
                Integer userId = Integer.parseInt(raw.replace("user_", "").split("_")[0]);
                User user = userService.findById(userId);
                if (user != null) {
                    // 检查用户状态，被禁用的用户返回401
                    if (user.getStatus() != null && user.getStatus() == 0) {
                        return Result.error(401, "账号已被禁用，请联系管理员");
                    }
                    user.setPassword(null);
                    return Result.success(user);
                }
            }
        } catch (Exception e) {
            // 解析失败
        }
        return Result.error(401, "登录已过期");
    }

    @PutMapping
    public Result<String> update(@RequestBody User user) {
        boolean success = userService.update(user);
        // 如果更新了头像，同步到 moments 和 comments 表
        if (success && user.getAvatar() != null) {
            userService.syncAvatarToMoments(user.getId().intValue(), user.getAvatar());
            userService.syncAvatarToComments(user.getId().intValue(), user.getAvatar());
        }
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestHeader(value = "Authorization", required = false) String token,
                                         @RequestBody Map<String, Object> data) {
        String oldPassword = (String) data.get("oldPassword");
        String newPassword = (String) data.get("newPassword");
        String confirmPassword = (String) data.get("confirmPassword");

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return Result.error("请输入原密码");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.error("请输入新密码");
        }
        if (newPassword.length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }
        if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
            return Result.error("两次输入的密码不一致");
        }

        // 从 token 中获取用户 ID
        if (token == null) return Result.error(401, "未登录");
        try {
            String raw = token.startsWith("Bearer ") ? token.substring(7) : token;
            if (raw.startsWith("guest_")) {
                return Result.error("游客无法修改密码，请先注册账号");
            }
            if (raw.startsWith("user_")) {
                Integer userId = Integer.parseInt(raw.replace("user_", "").split("_")[0]);
                User user = userService.findById(userId);
                if (user == null) {
                    return Result.error("用户不存在");
                }
                if (!oldPassword.equals(user.getPassword())) {
                    return Result.error("原密码错误");
                }
                // 更新密码
                user.setPassword(newPassword);
                boolean success = userService.update(user);
                return success ? Result.success("密码修改成功") : Result.error("密码修改失败");
            }
        } catch (Exception e) {
            return Result.error(401, "登录已过期，请重新登录");
        }
        return Result.error("修改失败");
    }



    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = userService.deleteById(id.intValue());
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/count")
    public Result<Integer> count() {
        return Result.success(userService.count());
    }
}
