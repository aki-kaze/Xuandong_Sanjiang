package org.example.sanjiang.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.sanjiang.entity.Moment;
import java.util.List;

public interface MomentMapper {
    List<Moment> findAll(@Param("searchKey") String searchKey, @Param("status") Integer status);
    List<Moment> findByStatus(Integer status);
    List<Moment> findByUserId(@Param("userId") Long userId);
    Moment findById(Long id);
    int insert(Moment moment);
    int update(Moment moment);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int deleteById(Long id);
    int updateLikes(@Param("id") Long id, @Param("delta") int delta);
    int updateComments(@Param("id") Long id, @Param("delta") int delta);
    int countAll();
    int countByStatus(@Param("status") Integer status);
}
