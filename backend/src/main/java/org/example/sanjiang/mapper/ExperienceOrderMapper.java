package org.example.sanjiang.mapper;

import org.example.sanjiang.entity.ExperienceOrder;
import java.util.List;

public interface ExperienceOrderMapper {
    List<ExperienceOrder> findAll();
    List<ExperienceOrder> findByCondition(@org.apache.ibatis.annotations.Param("status") String status, @org.apache.ibatis.annotations.Param("searchKey") String searchKey);
    List<ExperienceOrder> findByUserId(Long userId);
    List<ExperienceOrder> findByStatus(String status);
    ExperienceOrder findById(Long id);
    ExperienceOrder findByOrderNo(String orderNo);
    int insert(ExperienceOrder order);
    int update(ExperienceOrder order);
    int deleteById(Long id);
    int countAll();
    int countByCondition(@org.apache.ibatis.annotations.Param("status") String status, @org.apache.ibatis.annotations.Param("searchKey") String searchKey);
    int countByUserId(Long userId);
    int countByStatus(String status);
    int countToday();
    java.math.BigDecimal sumRevenue();
    List<ExperienceOrder> findRecent(int limit);
}
