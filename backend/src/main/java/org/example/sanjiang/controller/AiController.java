package org.example.sanjiang.controller;

import org.example.sanjiang.ai.MCPToolManager;
import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.AiChat;
import org.example.sanjiang.entity.AiChatSession;
import org.example.sanjiang.entity.AiKnowledge;
import org.example.sanjiang.mapper.AiChatMapper;
import org.example.sanjiang.mapper.AiChatSessionMapper;
import org.example.sanjiang.mapper.AiKnowledgeMapper;
import org.example.sanjiang.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiController {

    @Value("${qwen.api.key}")
    private String qwenApiKey;

    @Value("${qwen.api.url}")
    private String qwenApiUrl;

    @Value("${qwen.api.model}")
    private String qwenModel;

    @Autowired(required = false)
    private AiKnowledgeMapper aiKnowledgeMapper;

    @Autowired(required = false)
    private AiChatMapper aiChatMapper;

    @Autowired(required = false)
    private AiChatSessionMapper aiChatSessionMapper;

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Autowired
    private MCPToolManager mcpToolManager;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getCurrentDate() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy年M月d日");
        return today.format(formatter);
    }

    private String buildDefaultPrompt() {
        String currentDate = getCurrentDate();
        return "你是侗乡AI游览管家，主要服务于广西三江侗族自治县的旅游导览。" +
                "对于三江旅游相关问题，请给出详细专业的解答。" +
                "对于其他通用问题，也请正常回答，可以在回答末尾适当引导用户了解三江旅游。" +
                "回答请简洁友好，使用中文。" +
                "\n\n【重要信息】" +
                "\n- 今天是" + currentDate + "，请使用当前日期进行回答" +
                "\n- 对于天气查询，默认使用今天（" + currentDate + "）的天气信息" +
                "\n- 不要使用2024年或其他过时的时间信息" +
                "\n- 如果不确定具体天气，可以描述三江的气候特点和季节特征，不要编造具体日期的天气数据";
    }

    private String buildSystemPrompt() {
        if (aiKnowledgeMapper == null) return buildDefaultPrompt();
        try {
            List<AiKnowledge> prompts = aiKnowledgeMapper.findByCategory("prompt");
            List<AiKnowledge> knowledgeItems = aiKnowledgeMapper.findByCategory("knowledge");

            StringBuilder sb = new StringBuilder();
            if (prompts != null && !prompts.isEmpty()) {
                sb.append(prompts.stream().map(AiKnowledge::getContent).collect(Collectors.joining("\n")));
                sb.append("\n\n").append(buildDefaultPrompt());
            } else {
                sb.append(buildDefaultPrompt());
            }

            if (knowledgeItems != null && !knowledgeItems.isEmpty()) {
                sb.append("\n\n以下是你需要掌握的知识库内容：\n");
                for (AiKnowledge k : knowledgeItems) {
                    sb.append("【").append(k.getTitle()).append("】").append(k.getContent()).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return buildDefaultPrompt();
        }
    }

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        Long sessionId = null;
        AiChat savedChat = null;
        try {
            String message = (String) body.getOrDefault("message", "");
            List<Map<String, String>> history = (List<Map<String, String>>) body.getOrDefault("history", new ArrayList<>());
            Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
            String userName = (String) body.getOrDefault("userName", "未知用户");
            sessionId = body.get("session_id") != null ? Long.valueOf(body.get("session_id").toString()) : null;

            String reply;
            boolean isToolCall = false;
            Map<String, Object> toolData = null;

            if (mcpToolManager != null) {
                MCPToolManager.ToolResult toolResult = mcpToolManager.executeTool(message, new HashMap<>());
                if (toolResult != null && toolResult.isToolCall) {
                    reply = toolResult.content;
                    isToolCall = true;
                    toolData = new HashMap<>();
                    toolData.put("type", toolResult.type);
                    toolData.put("data", toolResult.data);
                    toolData.put("itinerary", toolResult.itinerary);
                } else {
                    reply = callQwenAPI(message, history);
                }
            } else {
                reply = callQwenAPI(message, history);
            }

            long responseTime = System.currentTimeMillis() - startTime;

            if (aiChatMapper != null && aiChatSessionMapper != null) {
                try {
                    if (sessionId == null) {
                        AiChatSession newSession = new AiChatSession();
                        newSession.setUserId(userId);
                        newSession.setUserName(userName);
                        newSession.setTitle(message.length() > 50 ? message.substring(0, 50) + "..." : message);
                        newSession.setLastMessage(reply.length() > 100 ? reply.substring(0, 100) + "..." : reply);
                        newSession.setMessageCount(1);
                        aiChatSessionMapper.insert(newSession);
                        sessionId = newSession.getId();
                    } else if (sessionId != null) {
                        aiChatSessionMapper.updateLastMessage(sessionId, reply.length() > 100 ? reply.substring(0, 100) + "..." : reply);
                    }

                    AiChat chat = new AiChat();
                    chat.setSessionId(sessionId);
                    chat.setUserId(userId);
                    chat.setUserName(userName);
                    chat.setQuestion(message);
                    chat.setAnswer(reply);
                    chat.setCategory(detectCategory(message));
                    chat.setResponseTime((int) responseTime);
                    chat.setTokens(0);

                    // 敏感词检测
                    System.out.println("[DEBUG] 开始敏感词检测, message=" + message);
                    List<String> detectedWords = sensitiveWordService.checkText(message);
                    System.out.println("[DEBUG] 敏感词检测结果: " + detectedWords);
                    if (detectedWords != null && !detectedWords.isEmpty()) {
                        chat.setHasSensitive(1);
                        chat.setSensitiveWords(detectedWords.toString());
                        chat.setFlagged(1);
                        // 更新每个敏感词的命中次数
                        for (String word : detectedWords) {
                            sensitiveWordService.incrementHitCount(word);
                        }
                    } else {
                        chat.setHasSensitive(0);
                        chat.setSensitiveWords("[]");
                        chat.setFlagged(0);
                    }

                    chat.setIp(getClientIp(request));
                    aiChatMapper.insert(chat);
                    savedChat = chat;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("reply", reply);
            if (sessionId != null) {
                result.put("session_id", sessionId);
            }
            if (savedChat != null) {
                result.put("chat_id", savedChat.getId());
            }
            if (isToolCall && toolData != null) {
                result.put("toolCall", true);
                result.put("toolType", toolData.get("type"));
                result.put("toolData", toolData.get("data"));
                result.put("itinerary", toolData.get("itinerary"));
            }
            return Result.success(result);

        } catch (Exception e) {
            return Result.error("AI服务暂时不可用：" + e.getMessage());
        }
    }

    private String callQwenAPI(String message, List<Map<String, String>> history) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> system = new HashMap<>();
            system.put("role", "system");
            system.put("content", buildSystemPrompt());
            messages.add(system);

            for (Map<String, String> h : history) {
                messages.add(h);
            }

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", message);
            messages.add(userMsg);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", qwenModel);
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 1024);
            requestBody.put("temperature", 0.7);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(qwenApiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(qwenApiUrl, entity, Map.class);

            Map responseBody = response.getBody();
            List<Map> choices = (List<Map>) responseBody.get("choices");
            Map firstChoice = choices.get(0);
            Map messageObj = (Map) firstChoice.get("message");
            return (String) messageObj.get("content");
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，AI服务暂时不可用，请稍后重试。";
        }
    }

    private String detectCategory(String message) {
        String msg = message.toLowerCase();
        if (msg.contains("路线") || msg.contains("行程") || msg.contains("怎么去") || msg.contains("几天") || msg.contains("安排")) {
            return "路线规划";
        }
        if (msg.contains("美食") || msg.contains("好吃") || msg.contains("餐厅") || msg.contains("吃")) {
            return "美食推荐";
        }
        if (msg.contains("景点") || msg.contains("好玩") || msg.contains("门票") || msg.contains("哪里")) {
            return "景点推荐";
        }
        if (msg.contains("文化") || msg.contains("历史") || msg.contains("侗族") || msg.contains("传统")) {
            return "文化咨询";
        }
        if (msg.contains("交通") || msg.contains("公交") || msg.contains("大巴") || msg.contains("开车")) {
            return "交通查询";
        }
        if (msg.contains("酒店") || msg.contains("住宿") || msg.contains("民宿")) {
            return "住宿推荐";
        }
        return "其他";
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @GetMapping("/knowledge")
    public Result<List<AiKnowledge>> getAllKnowledge() {
        if (aiKnowledgeMapper == null) return Result.success(new ArrayList<>());
        return Result.success(aiKnowledgeMapper.findAll());
    }

    @GetMapping("/knowledge/{id}")
    public Result<AiKnowledge> getKnowledgeById(@PathVariable Long id) {
        if (aiKnowledgeMapper == null) return Result.error("知识库服务不可用");
        return Result.success(aiKnowledgeMapper.findById(id));
    }

    @PostMapping("/knowledge")
    public Result<String> saveKnowledge(@RequestBody AiKnowledge knowledge) {
        if (aiKnowledgeMapper == null) return Result.error("知识库服务不可用");
        if (knowledge.getEnabled() == null) knowledge.setEnabled(1);
        if (knowledge.getSortOrder() == null) knowledge.setSortOrder(0);
        int result = aiKnowledgeMapper.insert(knowledge);
        return result > 0 ? Result.success("保存成功") : Result.error("保存失败");
    }

    @PutMapping("/knowledge")
    public Result<String> updateKnowledge(@RequestBody AiKnowledge knowledge) {
        if (aiKnowledgeMapper == null) return Result.error("知识库服务不可用");
        int result = aiKnowledgeMapper.updateWithVersion(knowledge);
        return result > 0 ? Result.success("更新成功") : Result.error("更新失败");
    }

    // 查询知识库版本历史
    @GetMapping("/knowledge/{id}/versions")
    public Result<Map<String, Object>> getVersions(@PathVariable Long id) {
        if (aiKnowledgeMapper == null) return Result.error("知识库服务不可用");
        return Result.success(aiKnowledgeMapper.findVersionHistory(id));
    }

    // 回滚到上一版本
    @PutMapping("/knowledge/{id}/rollback")
    public Result<String> rollback(@PathVariable Long id) {
        if (aiKnowledgeMapper == null) return Result.error("知识库服务不可用");
        int result = aiKnowledgeMapper.rollback(id);
        return result > 0 ? Result.success("回滚成功") : Result.error("回滚失败");
    }

    @DeleteMapping("/knowledge/{id}")
    public Result<String> deleteKnowledge(@PathVariable Long id) {
        if (aiKnowledgeMapper == null) return Result.error("知识库服务不可用");
        int result = aiKnowledgeMapper.deleteById(id);
        return result > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }
}
