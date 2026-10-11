package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.AiChat;
import org.example.sanjiang.mapper.AiChatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-chat")
@CrossOrigin
public class AiChatController {

    @Autowired
    private AiChatMapper aiChatMapper;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer flagged,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean today) {
        int offset = (page - 1) * size;
        List<AiChat> list = aiChatMapper.findByCondition(offset, size, flagged, keyword, today);
        int total = aiChatMapper.countByCondition(flagged, keyword, today);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        return Result.success(data);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", aiChatMapper.countAll());
        stats.put("todayCount", aiChatMapper.countToday());
        stats.put("flaggedCount", aiChatMapper.countFlagged());
        stats.put("categoryStats", aiChatMapper.countByCategory());
        stats.put("dailyTrend", aiChatMapper.countByDay());
        return Result.success(stats);
    }

    @GetMapping("/analytics")
    public Result<Map<String, Object>> getAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("categoryStats", aiChatMapper.categoryStatsLast7Days());
        analytics.put("dailyStats", aiChatMapper.dailyStatsLast30Days());
        analytics.put("avgResponseTime", aiChatMapper.avgResponseTimeLast7Days());
        return Result.success(analytics);
    }

    @GetMapping("/analytics/advanced")
    public Result<Map<String, Object>> getAdvancedAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        // 基础统计
        analytics.put("totalCount", aiChatMapper.countAll());
        analytics.put("todayCount", aiChatMapper.countToday());
        analytics.put("flaggedCount", aiChatMapper.countFlagged());

        // 评分统计
        analytics.put("ratingStats", aiChatMapper.ratingStats());
        analytics.put("categoryStats", aiChatMapper.categoryStatsLast7Days());
        analytics.put("dailyStats", aiChatMapper.dailyStatsLast30Days());
        analytics.put("avgResponseTime", aiChatMapper.avgResponseTimeLast7Days());

        // 用户行为分析
        analytics.put("userActivity", aiChatMapper.userActivityLast7Days());
        analytics.put("hotQuestions", aiChatMapper.hotQuestionsLast30Days());
        analytics.put("hourlyStats", aiChatMapper.hourlyStatsLast7Days());

        // 知识库效果分析
        analytics.put("knowledgeUsage", aiChatMapper.knowledgeUsageEffect());

        // 性能分析
        analytics.put("responseTimeDistribution", aiChatMapper.responseTimeDistribution());
        analytics.put("tokenUsage", aiChatMapper.tokenUsageStats());

        // 问题分析
        analytics.put("questionLength", aiChatMapper.questionLengthAnalysis());
        analytics.put("sensitiveWords", aiChatMapper.sensitiveWordsStats());

        return Result.success(analytics);
    }

    @PutMapping("/{id}/flag")
    public Result<String> toggleFlag(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer flagged = body.getOrDefault("flagged", 1);
        aiChatMapper.updateFlagged(id, flagged);
        return Result.success("操作成功");
    }

    // 用户评分接口
    @PutMapping("/{id}/rate")
    public Result<String> rateChat(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer rating = body.get("rating"); // 1=不满意, 2=满意
        aiChatMapper.updateRating(id, rating);
        return Result.success("评分成功");
    }

    // 热门问题 Top 10
    @GetMapping("/hot-questions")
    public Result<List<Map<String, Object>>> hotQuestions() {
        return Result.success(aiChatMapper.hotQuestionsTop10());
    }

    // 低分/差评对话
    @GetMapping("/low-rated")
    public Result<List<Map<String, Object>>> lowRated() {
        return Result.success(aiChatMapper.lowRatedChats());
    }

    // 评分统计
    @GetMapping("/rating-stats")
    public Result<Map<String, Object>> ratingStats() {
        return Result.success(aiChatMapper.ratingStats());
    }

    @PostMapping("/save")
    public Result<String> saveChat(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            AiChat chat = new AiChat();
            chat.setSessionId(body.get("session_id") != null ? Long.valueOf(body.get("session_id").toString()) : null);
            chat.setUserId(body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null);
            chat.setUserName(body.get("userName") != null ? body.get("userName").toString() : "未知用户");
            chat.setQuestion(body.get("question") != null ? body.get("question").toString() : "");
            chat.setAnswer(body.get("answer") != null ? body.get("answer").toString() : "");
            chat.setCategory(body.get("category") != null ? body.get("category").toString() : "其他");
            chat.setResponseTime(body.get("response_time") != null ? Integer.valueOf(body.get("response_time").toString()) : 0);
            chat.setTokens(0);
            chat.setHasSensitive(0);
            chat.setSensitiveWords("[]");
            chat.setFlagged(0);
            chat.setIp(request.getRemoteAddr());
            aiChatMapper.insert(chat);
            return Result.success("保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("保存失败：" + e.getMessage());
        }
    }
}
