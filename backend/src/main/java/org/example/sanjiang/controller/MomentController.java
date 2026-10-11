package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.Moment;
import org.example.sanjiang.service.MomentService;
import org.example.sanjiang.service.SensitiveWordService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/moments")
@CrossOrigin
public class MomentController {

    @Autowired
    private MomentService momentService;
    
    @Autowired(required = false)
    private SensitiveWordService sensitiveWordService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        List<Moment> list = momentService.findAll(page, size, keyword, status);
        int total = (int) new PageInfo<>(list).getTotal();

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Moment> getById(@PathVariable Long id) {
        Moment moment = momentService.findById(id);
        return Result.success(moment);
    }

    @PostMapping
    public Result<String> publish(@RequestBody Map<String, Object> params) {
        // 敏感词检测
        if (sensitiveWordService != null) {
            String content = (String) params.get("content");
            if (content != null && !content.trim().isEmpty()) {
                List<String> sensitiveWords = sensitiveWordService.checkText(content);
                if (!sensitiveWords.isEmpty()) {
                    return Result.error("内容包含敏感词：" + String.join("、", sensitiveWords));
                }
            }
        }
        
        Moment moment = new Moment();
        moment.setUserId(params.get("user_id") != null ? Long.valueOf(params.get("user_id").toString()) : 0L);
        moment.setNickname((String) params.get("nickname"));
        moment.setAvatar((String) params.get("avatar"));
        moment.setContent((String) params.get("content"));
        moment.setCoverImg((String) params.get("cover_img"));
        Object images = params.get("images");
        moment.setImages(images != null ? images.toString() : null);
        Object tags = params.get("tags");
        moment.setTags(tags != null ? tags.toString() : "[]");
        moment.setStatus(0); // 新帖子默认待审核，需要管理平台审核后才显示
        moment.setLikes(0);
        moment.setComments(0);

        boolean success = momentService.save(moment);
        return success ? Result.success("发布成功，待审核") : Result.error("发布失败");
    }

    @PostMapping("/like/{id}")
    public Result<String> like(@PathVariable Long id) {
        boolean success = momentService.updateLikes(id, 1);
        return success ? Result.success("点赞成功") : Result.error("点赞失败");
    }

    @PostMapping("/approve/{id}")
    public Result<String> approve(@PathVariable Long id) {
        boolean success = momentService.approve(id);
        return success ? Result.success("审核通过") : Result.error("审核失败");
    }

    @PostMapping("/reject/{id}")
    public Result<String> reject(@PathVariable Long id) {
        boolean success = momentService.reject(id);
        return success ? Result.success("审核拒绝") : Result.error("操作失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = momentService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/count/pending")
    public Result<Integer> countPending() {
        int count = momentService.countByStatus(0);
        return Result.success(count);
    }

    /**
     * 获取当前用户的帖子列表（包括待审核的）
     */
    @GetMapping("/my")
    public Result<Map<String, Object>> getMyPosts(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Moment> list = momentService.findByUserId(userId, page, size);
        int total = (int) new PageInfo<>(list).getTotal();

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }
}
