package org.example.sanjiang.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.sanjiang.entity.User;
import java.util.List;

public interface UserMapper {
    List<User> findAll();
    List<User> search(String keyword);
    User findById(Integer id);
    User findByOpenid(String openid);
    User findByPhone(String phone);
    int insert(User user);
    int update(User user);
    int deleteById(Integer id);
    int countAll();
    int countSearch(String keyword);
    int updateMomentsAvatar(@Param("userId") Integer userId, @Param("avatar") String avatar);
    int updateCommentsAvatar(@Param("userId") Integer userId, @Param("avatar") String avatar);
    int countToday();
}
