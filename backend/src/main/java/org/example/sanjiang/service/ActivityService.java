package org.example.sanjiang.service;

import org.example.sanjiang.entity.Activity;
import java.util.List;

public interface ActivityService {
    List<Activity> findAll(int page, int size);
    List<Activity> findByStatus(String status, int page, int size);
    List<Activity> findPublished(int page, int size); // 查询已上架的活动
    Activity findById(Long id);
    boolean save(Activity activity);
    boolean update(Activity activity);
    boolean publish(Long id); // 上架
    boolean unpublish(Long id); // 下架
    boolean deleteById(Long id);
    int count();
}
