package org.example.sanjiang.mapper;

import org.apache.ibatis.annotations.*;
import org.example.sanjiang.entity.AiKnowledge;
import java.util.List;
import java.util.Map;

@Mapper
public interface AiKnowledgeMapper {

    @Select("SELECT * FROM ai_knowledge ORDER BY sort_order ASC, id ASC")
    List<AiKnowledge> findAll();

    @Select("SELECT * FROM ai_knowledge WHERE category = #{category} AND enabled = 1 ORDER BY sort_order ASC")
    List<AiKnowledge> findByCategory(String category);

    @Select("SELECT * FROM ai_knowledge WHERE id = #{id}")
    AiKnowledge findById(Long id);

    @Insert("INSERT INTO ai_knowledge(category, title, content, enabled, sort_order) " +
            "VALUES(#{category}, #{title}, #{content}, #{enabled}, #{sortOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiKnowledge knowledge);

    @Update("UPDATE ai_knowledge SET category=#{category}, title=#{title}, content=#{content}, " +
            "enabled=#{enabled}, sort_order=#{sortOrder} WHERE id=#{id}")
    int update(AiKnowledge knowledge);

    // 更新时保存旧版本内容到original_content，版本号+1
    @Update("UPDATE ai_knowledge SET category=#{category}, title=#{title}, content=#{content}, " +
            "enabled=#{enabled}, sort_order=#{sortOrder}, " +
            "original_content = CASE WHEN version = 0 THEN content ELSE original_content END, " +
            "version = version + 1 WHERE id=#{id}")
    int updateWithVersion(AiKnowledge knowledge);

    // 查询版本历史
    @Select("SELECT id, title, content, original_content, version, create_time, update_time " +
            "FROM ai_knowledge WHERE id = #{id}")
    Map<String, Object> findVersionHistory(@Param("id") Long id);

    // 回滚到上一版本
    @Update("UPDATE ai_knowledge SET content = original_content, version = version + 1 " +
            "WHERE id = #{id} AND original_content IS NOT NULL AND original_content != ''")
    int rollback(@Param("id") Long id);

    @Delete("DELETE FROM ai_knowledge WHERE id = #{id}")
    int deleteById(Long id);

    // 分页搜索：关键词+分类筛选
    @Select("<script>" +
            "SELECT * FROM ai_knowledge " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>AND (title LIKE CONCAT('%',#{keyword},'%') OR content LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "<if test='category != null and category != \"\"'>AND category = #{category}</if>" +
            "</where>" +
            "ORDER BY sort_order ASC, id ASC" +
            "</script>")
    List<AiKnowledge> findBySearch(@Param("keyword") String keyword, @Param("category") String category);

    // 统计：关键词+分类筛选
    @Select("<script>" +
            "SELECT COUNT(*) FROM ai_knowledge " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>AND (title LIKE CONCAT('%',#{keyword},'%') OR content LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "<if test='category != null and category != \"\"'>AND category = #{category}</if>" +
            "</where>" +
            "</script>")
    int countBySearch(@Param("keyword") String keyword, @Param("category") String category);

    // 统计全部
    @Select("SELECT COUNT(*) FROM ai_knowledge")
    int countAll();

    // 批量更新启用状态
    @Update("<script>" +
            "UPDATE ai_knowledge SET enabled = #{enabled} WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateEnabled(@Param("ids") List<Long> ids, @Param("enabled") Integer enabled);

    // 批量删除
    @Delete("<script>" +
            "DELETE FROM ai_knowledge WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDeleteByIds(@Param("ids") List<Long> ids);
}
