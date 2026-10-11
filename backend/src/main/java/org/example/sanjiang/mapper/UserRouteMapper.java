package org.example.sanjiang.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.sanjiang.entity.UserRoute;
import java.util.List;

public interface UserRouteMapper {
    List<UserRoute> findAll();
    List<UserRoute> findByUserId(Integer userId);
    List<UserRoute> findByConditions(@Param("keyword") String keyword,
                                     @Param("userId") Integer userId,
                                     @Param("status") String status);
    UserRoute findById(Integer id);
    int insert(UserRoute userRoute);
    int update(UserRoute userRoute);
    int deleteById(Integer id);
    int countAll();
}
