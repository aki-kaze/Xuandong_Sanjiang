package org.example.sanjiang.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.sanjiang.entity.Post;
import java.util.List;

public interface PostMapper {
    List<Post> findAll(@Param("searchKey") String searchKey, @Param("status") Integer status);
    List<Post> findByUserId(Long userId);
    Post findById(Long id);
    int insert(Post post);
    int update(Post post);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int updateRecommendation(@Param("id") Long id, @Param("isRecommended") Integer isRecommended);
    int deleteById(Long id);
    int countAll();
    int countByStatus(@Param("status") Integer status);
}
