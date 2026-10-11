package org.example.sanjiang.mapper;

import org.apache.ibatis.annotations.*;
import org.example.sanjiang.entity.AiChat;
import java.util.List;
import java.util.Map;

@Mapper
public interface AiChatMapper {

    @Insert("INSERT INTO ai_chat(session_id, user_id, user_name, question, answer, category, response_time, tokens, has_sensitive, sensitive_words, flagged, ip) " +
            "VALUES(#{sessionId}, #{userId}, #{userName}, #{question}, #{answer}, #{category}, #{responseTime}, #{tokens}, #{hasSensitive}, #{sensitiveWords}, #{flagged}, #{ip})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiChat chat);

    @Select("SELECT * FROM ai_chat WHERE session_id = #{sessionId} ORDER BY create_time ASC")
    List<AiChat> findBySessionId(@Param("sessionId") Long sessionId);

    List<AiChat> findByCondition(@Param("offset") int offset, @Param("size") int size,
                                  @Param("flagged") Integer flagged, @Param("keyword") String keyword,
                                  @Param("today") Boolean today);

    int countByCondition(@Param("flagged") Integer flagged, @Param("keyword") String keyword,
                         @Param("today") Boolean today);

    @Select("SELECT COUNT(*) FROM ai_chat")
    int countAll();

    @Select("SELECT COUNT(*) FROM ai_chat WHERE DATE(create_time) = CURDATE()")
    int countToday();

    @Select("SELECT COUNT(*) FROM ai_chat WHERE flagged = 1")
    int countFlagged();

    @Update("UPDATE ai_chat SET flagged = #{flagged} WHERE id = #{id}")
    int updateFlagged(@Param("id") Long id, @Param("flagged") Integer flagged);

    @Select("SELECT category, COUNT(*) as count FROM ai_chat GROUP BY category ORDER BY count DESC")
    List<Map<String, Object>> countByCategory();

    @Select("SELECT DATE(create_time) as date, COUNT(*) as count FROM ai_chat " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> countByDay();

    @Select("SELECT category, COUNT(*) as count FROM ai_chat " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY category ORDER BY count DESC LIMIT 10")
    List<Map<String, Object>> categoryStatsLast7Days();

    @Select("SELECT AVG(response_time) as avg_time FROM ai_chat WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)")
    Map<String, Object> avgResponseTimeLast7Days();

    @Select("SELECT DATE(create_time) as date, COUNT(*) as count, " +
            "AVG(response_time) as avg_time, SUM(tokens) as total_tokens " +
            "FROM ai_chat WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> dailyStatsLast30Days();

    // 新增：用户活跃度分析
    @Select("SELECT user_id, user_name, COUNT(*) as chat_count, " +
            "AVG(response_time) as avg_response_time, MAX(create_time) as last_chat_time " +
            "FROM ai_chat WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY user_id, user_name ORDER BY chat_count DESC LIMIT 20")
    List<Map<String, Object>> userActivityLast7Days();

    // 新增：热门问题分析
    @Select("SELECT question, COUNT(*) as frequency, category, " +
            "AVG(response_time) as avg_response_time " +
            "FROM ai_chat WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "AND LENGTH(question) > 5 " +
            "GROUP BY question, category ORDER BY frequency DESC LIMIT 10")
    List<Map<String, Object>> hotQuestionsLast30Days();

    // 新增：知识库使用效果分析
    @Select("SELECT k.title as knowledge_title, k.category as knowledge_category, " +
            "COUNT(ac.id) as usage_count, AVG(ac.response_time) as avg_response_time, " +
            "SUM(CASE WHEN ac.has_sensitive = 1 THEN 1 ELSE 0 END) as sensitive_count " +
            "FROM ai_knowledge k " +
            "LEFT JOIN ai_chat ac ON ac.create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "WHERE k.enabled = 1 " +
            "GROUP BY k.id, k.title, k.category " +
            "ORDER BY usage_count DESC LIMIT 10")
    List<Map<String, Object>> knowledgeUsageEffect();

    // 新增：时段分析（按小时统计）
    @Select("SELECT HOUR(create_time) as hour, COUNT(*) as count, " +
            "AVG(response_time) as avg_response_time " +
            "FROM ai_chat WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY HOUR(create_time) ORDER BY hour")
    List<Map<String, Object>> hourlyStatsLast7Days();

    // 新增：敏感词统计
    @Select("SELECT sensitive_words, COUNT(*) as count " +
            "FROM ai_chat WHERE has_sensitive = 1 " +
            "AND create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY sensitive_words ORDER BY count DESC LIMIT 10")
    List<Map<String, Object>> sensitiveWordsStats();

    // 新增：Token使用统计
    @Select("SELECT DATE(create_time) as date, " +
            "SUM(tokens) as total_tokens, " +
            "AVG(tokens) as avg_tokens, " +
            "COUNT(*) as chat_count " +
            "FROM ai_chat WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> tokenUsageStats();

    // 新增：响应时间分布
    @Select("SELECT " +
            "CASE " +
            "WHEN response_time < 1000 THEN '1秒内' " +
            "WHEN response_time < 3000 THEN '1-3秒' " +
            "WHEN response_time < 5000 THEN '3-5秒' " +
            "WHEN response_time < 10000 THEN '5-10秒' " +
            "ELSE '10秒以上' " +
            "END as time_range, " +
            "COUNT(*) as count " +
            "FROM ai_chat WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY time_range ORDER BY MIN(response_time)")
    List<Map<String, Object>> responseTimeDistribution();

    // 新增：问题长度分析
    @Select("SELECT " +
            "CASE " +
            "WHEN LENGTH(question) <= 10 THEN '短问题(≤10字)' " +
            "WHEN LENGTH(question) <= 30 THEN '中等问题(11-30字)' " +
            "WHEN LENGTH(question) <= 100 THEN '长问题(31-100字)' " +
            "ELSE '超长问题(>100字)' " +
            "END as length_range, " +
            "COUNT(*) as count, " +
            "AVG(response_time) as avg_response_time " +
            "FROM ai_chat WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY length_range ORDER BY MIN(LENGTH(question))")
    List<Map<String, Object>> questionLengthAnalysis();

    // 更新评分
    @Update("UPDATE ai_chat SET rating = #{rating} WHERE id = #{id}")
    int updateRating(@Param("id") Long id, @Param("rating") Integer rating);

    // 热门问题 Top 10（30天内，按频率排序）
    @Select("SELECT question, COUNT(*) as frequency, category, " +
            "AVG(response_time) as avg_response_time, " +
            "SUM(CASE WHEN rating = 1 THEN 1 ELSE 0 END) as dislike_count " +
            "FROM ai_chat WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "AND LENGTH(question) > 5 " +
            "GROUP BY question, category ORDER BY frequency DESC LIMIT 10")
    List<Map<String, Object>> hotQuestionsTop10();

    // 低分/差评对话（用户标记不满意的）
    @Select("SELECT id, question, answer, rating, category, create_time, response_time " +
            "FROM ai_chat WHERE rating = 1 " +
            "ORDER BY create_time DESC LIMIT 20")
    List<Map<String, Object>> lowRatedChats();

    // 评分统计
    @Select("SELECT " +
            "SUM(CASE WHEN rating = 2 THEN 1 ELSE 0 END) as satisfied_count, " +
            "SUM(CASE WHEN rating = 1 THEN 1 ELSE 0 END) as dissatisfied_count, " +
            "SUM(CASE WHEN rating > 0 THEN 1 ELSE 0 END) as total_rated " +
            "FROM ai_chat")
    Map<String, Object> ratingStats();
}
