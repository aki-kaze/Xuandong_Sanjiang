package org.example.sanjiang.service;

import org.example.sanjiang.entity.UserRoute;
import java.util.List;

public interface UserRouteService {
    List<UserRoute> findAll(int page, int size);
    List<UserRoute> findByConditions(int page, int size, String keyword, Integer userId, String status);
    List<UserRoute> findByUserId(Integer userId);
    UserRoute findById(Integer id);
    boolean save(UserRoute userRoute);
    boolean update(UserRoute userRoute);
    boolean deleteById(Integer id);
    int count();

    /**
     * 检查路线描述是否包含敏感词
     * @param description 路线描述
     * @return 包含的敏感词列表，空列表表示无敏感词
     */
    List<String> checkRouteDescription(String description);
}
