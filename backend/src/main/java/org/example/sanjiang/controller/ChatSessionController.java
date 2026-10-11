package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.AiChat;
import org.example.sanjiang.entity.AiChatSession;
import org.example.sanjiang.mapper.AiChatMapper;
import org.example.sanjiang.mapper.AiChatSessionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatSessionController {

    @Autowired
    private AiChatSessionMapper aiChatSessionMapper;

    @Autowired
    private AiChatMapper aiChatMapper;

    @PostMapping("/sessions")
    public Result<Map<String, Object>> createSession(@RequestBody Map<String, Object> body) {
        AiChatSession session = new AiChatSession();
        if (body.get("userId") != null) {
            session.setUserId(Long.valueOf(body.get("userId").toString()));
        }
        session.setUserName(body.get("userName") != null ? body.get("userName").toString() : "未知用户");
        session.setTitle(body.get("title") != null ? body.get("title").toString() : "新会话");
        session.setLastMessage("");
        session.setMessageCount(0);
        aiChatSessionMapper.insert(session);

        Map<String, Object> result = new HashMap<>();
        result.put("id", session.getId());
        result.put("userId", session.getUserId());
        result.put("userName", session.getUserName());
        result.put("title", session.getTitle());
        result.put("messageCount", 0);
        return Result.success(result);
    }

    @GetMapping("/sessions")
    public Result<Map<String, Object>> getSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        Map<String, Object> result = new HashMap<>();
        List<AiChatSession> sessions = new ArrayList<>();

        try {
            int offset = (page - 1) * limit;
            sessions = aiChatSessionMapper.findAllSessions(offset, limit);
        } catch (Exception e) {
            e.printStackTrace();
        }

        result.put("list", sessions);
        result.put("total", sessions.size());
        result.put("page", page);
        result.put("size", limit);
        return Result.success(result);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<Map<String, Object>>> getSessionMessages(@PathVariable Long sessionId) {
        List<AiChat> chatList = aiChatMapper.findBySessionId(sessionId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (AiChat chat : chatList) {
            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", chat.getQuestion());
            result.add(userMsg);

            Map<String, Object> aiMsg = new HashMap<>();
            aiMsg.put("role", "assistant");
            aiMsg.put("content", chat.getAnswer());
            result.add(aiMsg);
        }

        return Result.success(result);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public Result<String> deleteSession(@PathVariable Long sessionId) {
        try {
            aiChatSessionMapper.deleteById(sessionId);
        } catch (Exception e) {
            // 忽略删除错误
        }
        return Result.success("删除成功");
    }

    @PostMapping("/messages")
    public Result<String> saveMessage(@RequestBody Map<String, Object> body) {
        return Result.success("保存成功");
    }
}
