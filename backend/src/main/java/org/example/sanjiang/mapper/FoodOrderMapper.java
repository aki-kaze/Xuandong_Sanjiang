package org.example.sanjiang.mapper;

import org.example.sanjiang.entity.FoodOrder;
import java.util.List;

public interface FoodOrderMapper {
    List<FoodOrder> findAll();
    List<FoodOrder> findByCondition(String status, String searchKey);
    List<FoodOrder> findByUserId(Long userId);
    List<FoodOrder> findByStatus(String status);
    FoodOrder findById(Long id);
    FoodOrder findByOrderNo(String orderNo);
    int insert(FoodOrder order);
    int update(FoodOrder order);
    int deleteById(Long id);
    int countAll();
    int countByCondition(String status, String searchKey);
    int countByUserId(Long userId);
    int countByStatus(String status);
    int countToday();
    int countTodayByStatus(String status);
    java.math.BigDecimal sumTotalPriceByStatus(java.util.List<String> statuses);
    java.math.BigDecimal sumRevenue();
    List<org.example.sanjiang.entity.FoodOrder> findRecent(int limit);
}
