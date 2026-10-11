package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.Post;
import org.example.sanjiang.entity.Comment;
import org.example.sanjiang.service.PostService;
import org.example.sanjiang.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        List<Post> list = postService.findAll(page, size, keyword, status);
        int total = postService.count();

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Post> getById(@PathVariable Long id) {
        Post post = postService.findById(id);
        return Result.success(post);
    }

    /**
     * 获取帖子的评论列表
     */
    @GetMapping("/{id}/comments")
    public Result<Map<String, Object>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        List<Comment> comments;
        if (status != null) {
            comments = commentService.findByPostIdAndStatus(id, status);
        } else {
            comments = commentService.findByPostId(id);
        }
        int total = commentService.countByPostId(id);

        Map<String, Object> result = new HashMap<>();
        result.put("records", comments);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    /**
     * 发表评论
     */
    @PostMapping("/{id}/comments")
    public Result<String> addComment(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Object userIdObj = params.get("user_id");
        if (userIdObj == null) {
            return Result.error("请先登录后再发表评论");
        }
        Comment comment = new Comment();
        comment.setPostId(id);
        comment.setUserId(Long.valueOf(userIdObj.toString()));
        comment.setNickname((String) params.get("nickname"));
        comment.setAvatar((String) params.get("avatar"));
        comment.setContent((String) params.get("content"));
        comment.setLikes(0);
        comment.setStatus(1); // 评论不需要审核，直接显示

        boolean success = commentService.save(comment);
        if (success) {
            // 更新帖子评论数
            Post post = postService.findById(id);
            if (post != null) {
                post.setComments(post.getComments() + 1);
                postService.update(post);
            }
        }
        return success ? Result.success("评论成功") : Result.error("评论失败");
    }

    @PostMapping
    public Result<String> save(@RequestBody Map<String, Object> params) {
        Post post = new Post();
        post.setUserId(Long.valueOf(params.get("user_id").toString()));
        String nickname = (String) params.get("nickname");
        post.setAuthor(nickname);
        post.setAvatar((String) params.get("avatar"));
        post.setContent((String) params.get("content"));
        post.setCoverImg((String) params.get("cover_img"));
        Object tags = params.get("tags");
        post.setTags(tags != null ? tags.toString() : "[]");
        post.setStatus(0); // 新帖子默认待审核
        post.setLikes(0);
        post.setComments(0);

        boolean success = postService.save(post);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Post post) {
        boolean success = postService.update(post);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = postService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PostMapping("/approve/{id}")
    public Result<String> approve(@PathVariable Long id) {
        boolean success = postService.approve(id);
        return success ? Result.success("审核通过") : Result.error("审核失败");
    }

    @PostMapping("/reject/{id}")
    public Result<String> reject(@PathVariable Long id) {
        boolean success = postService.reject(id);
        return success ? Result.success("审核拒绝") : Result.error("审核失败");
    }

    @PostMapping("/recommend/{id}")
    public Result<String> recommend(@PathVariable Long id, @RequestParam Integer isRecommended) {
        boolean success = postService.toggleRecommend(id, isRecommended);
        return success ? Result.success("操作成功") : Result.error("操作失败");
    }

    @GetMapping("/count/pending")
    public Result<Integer> countPending() {
        return Result.success(postService.countPending());
    }
}
