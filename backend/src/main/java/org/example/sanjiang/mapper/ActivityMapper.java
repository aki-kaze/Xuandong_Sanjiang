package org.example.sanjiang.mapper;

import org.example.sanjiang.entity.Activity;
import java.util.List;

public interface ActivityMapper {
    List<Activity> findAll();
    List<Activity> findByStatus(String status);
    List<Activity> findPublished(); // 查询已上架的活动
    Activity findById(Long id);
    int insert(Activity activity);
    int update(Activity activity);
    int updatePublishStatus(Long id, Integer isPublished); // 更新上架状态
    int deleteById(Long id);
    int countAll();
}
