package org.example.sanjiang.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.sanjiang.entity.MiniProgramChatSession;
import org.example.sanjiang.entity.MiniProgramChatMessage;
import java.util.List;
import java.util.Map;

@Mapper
public interface MiniProgramChatMapper {

    // 会话查询
    List<MiniProgramChatSession> findSessionsByCondition(@Param("offset") int offset,
                                                          @Param("size") int size,
                                                          @Param("keyword") String keyword,
                                                          @Param("today") Boolean today);

    int countSessionsByCondition(@Param("keyword") String keyword,
                                 @Param("today") Boolean today);

    // 消息查询
    List<MiniProgramChatMessage> findMessagesBySessionId(@Param("sessionId") Long sessionId);

    // 统计
    int countAllSessions();

    int countTodaySessions();

    int countAllMessages();

    int countTodayMessages();

    List<Map<String, Object>> countMessagesByRole();

    List<Map<String, Object>> toolUsageStats();

    List<Map<String, Object>> dailySessionStats(@Param("days") int days);
}
