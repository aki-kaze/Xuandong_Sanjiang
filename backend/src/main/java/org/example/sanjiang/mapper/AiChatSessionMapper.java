package org.example.sanjiang.mapper;

import org.apache.ibatis.annotations.*;
import org.example.sanjiang.entity.AiChatSession;
import java.util.List;

@Mapper
public interface AiChatSessionMapper {

    @Insert("INSERT INTO ai_chat_session(user_id, user_name, title, last_message, message_count) " +
            "VALUES(#{userId}, #{userName}, #{title}, #{lastMessage}, #{messageCount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiChatSession session);

    @Update("UPDATE ai_chat_session SET last_message = #{lastMessage}, message_count = message_count + 1, " +
            "update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateLastMessage(@Param("id") Long id, @Param("lastMessage") String lastMessage);

    @Select("SELECT * FROM ai_chat_session WHERE user_id = #{userId} ORDER BY update_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<AiChatSession> findByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM ai_chat_session WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM ai_chat_session ORDER BY update_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<AiChatSession> findAllSessions(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT * FROM ai_chat_session WHERE id = #{id}")
    AiChatSession findById(@Param("id") Long id);

    @Delete("DELETE FROM ai_chat_session WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Delete("DELETE FROM ai_chat WHERE id IN (SELECT id FROM ai_chat WHERE user_id = #{userId} AND session_id = #{sessionId})")
    int deleteMessagesBySessionId(@Param("userId") Long userId, @Param("sessionId") Long sessionId);
}
