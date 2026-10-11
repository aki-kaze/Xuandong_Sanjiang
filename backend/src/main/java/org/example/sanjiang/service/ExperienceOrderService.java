package org.example.sanjiang.service;

import org.example.sanjiang.entity.ExperienceOrder;
import java.util.List;
import java.util.Map;

public interface ExperienceOrderService {
    List<ExperienceOrder> findAll(int page, int size);
    List<ExperienceOrder> findByCondition(String status, String searchKey, int page, int size);
    List<ExperienceOrder> findByUserId(Long userId);
    List<ExperienceOrder> findByUserId(Long userId, int page, int size);
    ExperienceOrder findById(Long id);
    ExperienceOrder findByOrderNo(String orderNo);
    Map<String, Object> create(ExperienceOrder order);
    boolean confirm(String orderNo);
    boolean complete(String orderNo);
    boolean cancel(String orderNo, String reason);
    boolean update(ExperienceOrder order);
    boolean deleteById(Long id);
    Map<String, Object> getStats();
    int count();
    int countByCondition(String status, String searchKey);
    int countByUserId(Long userId);
}
