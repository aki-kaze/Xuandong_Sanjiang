package org.example.sanjiang.mapper;

import org.apache.ibatis.annotations.*;
import org.example.sanjiang.entity.Comment;
import java.util.List;

@Mapper
public interface CommentMapper {

    @Select("SELECT c.*, u.nickname, u.avatar FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE c.post_id = #{postId} AND c.status = 1 " +
            "ORDER BY c.created_at DESC")
    List<Comment> findByPostId(@Param("postId") Long postId);

    @Select("SELECT c.*, u.nickname, u.avatar FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE c.post_id = #{postId} AND c.status = #{status} " +
            "ORDER BY c.created_at DESC")
    List<Comment> findByPostIdAndStatus(@Param("postId") Long postId, @Param("status") Integer status);

    @Select("SELECT c.*, u.nickname, u.avatar FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "ORDER BY c.created_at DESC " +
            "LIMIT #{limit}")
    List<Comment> findRecent(@Param("limit") int limit);

    @Insert("INSERT INTO comments (post_id, user_id, nickname, avatar, content, likes, status) " +
            "VALUES (#{postId}, #{userId}, #{nickname}, #{avatar}, #{content}, #{likes}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    @Update("UPDATE comments SET content = #{content} WHERE id = #{id}")
    int update(Comment comment);

    @Update("UPDATE comments SET status = 0 WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Update("UPDATE comments SET likes = likes + 1 WHERE id = #{id}")
    int incrementLikes(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM comments WHERE post_id = #{postId} AND status = 1")
    int countByPostId(@Param("postId") Long postId);

    @Select("SELECT COUNT(*) FROM comments WHERE status = 1")
    int countAll();

    @Select("<script>" +
            "SELECT COUNT(*) FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE 1=1 " +
            "<if test='status != null'>AND c.status = #{status}</if> " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (c.content LIKE CONCAT('%', #{keyword}, '%') OR u.nickname LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if> " +
            "</script>")
    int countAllWithFilter(@Param("status") Integer status, @Param("keyword") String keyword);

    @Update("UPDATE comments SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("<script>" +
            "SELECT c.*, u.nickname, u.avatar FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE 1=1 " +
            "<if test='status != null'>AND c.status = #{status}</if> " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (c.content LIKE CONCAT('%', #{keyword}, '%') OR u.nickname LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if> " +
            "ORDER BY c.created_at DESC" +
            "</script>")
    List<Comment> findAllWithFilter(@Param("status") Integer status, @Param("keyword") String keyword);
}
