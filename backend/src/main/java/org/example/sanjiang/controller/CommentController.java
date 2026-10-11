package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.Comment;
import org.example.sanjiang.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 获取帖子的评论列表
     */
    @GetMapping("/post/{postId}")
    public Result<Map<String, Object>> getByPostId(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Comment> list = commentService.findByPostId(postId);
        int total = commentService.countByPostId(postId);

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    /**
     * 获取最新评论（用于管理后台监控）
     */
    @GetMapping("/recent")
    public Result<List<Comment>> getRecent(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(commentService.findRecent(limit));
    }

    /**
     * 发表评论
     */
    @PostMapping
    public Result<String> save(@RequestBody Comment comment) {
        boolean success = commentService.save(comment);
        return success ? Result.success("评论成功") : Result.error("评论失败");
    }

    /**
     * 删除评论（管理后台使用）
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = commentService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 点赞评论
     */
    @PostMapping("/{id}/like")
    public Result<String> like(@PathVariable Long id) {
        boolean success = commentService.incrementLikes(id);
        return success ? Result.success("点赞成功") : Result.error("点赞失败");
    }

    /**
     * 获取评论总数
     */
    @GetMapping("/count")
    public Result<Integer> count() {
        return Result.success(commentService.countAll());
    }

    /**
     * 审核通过评论
     */
    @PostMapping("/{id}/approve")
    public Result<String> approve(@PathVariable Long id) {
        boolean success = commentService.updateStatus(id, 1);
        return success ? Result.success("审核通过") : Result.error("操作失败");
    }

    /**
     * 审核拒绝/隐藏评论
     */
    @PostMapping("/{id}/reject")
    public Result<String> reject(@PathVariable Long id) {
        boolean success = commentService.updateStatus(id, 2);
        return success ? Result.success("已拒绝该评论") : Result.error("操作失败");
    }

    /**
     * 获取所有评论（管理后台使用，支持分页和筛选）
     */
    @GetMapping("/admin")
    public Result<Map<String, Object>> getAllForAdmin(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        List<Comment> list = commentService.findAllWithFilter(status, keyword, page, size);
        int total = commentService.countAllWithFilter(status, keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }
}
