package org.example.sanjiang.service;

import org.example.sanjiang.entity.FoodOrder;
import java.util.List;
import java.util.Map;

public interface FoodOrderService {
    List<FoodOrder> findAll(int page, int size);
    List<FoodOrder> findAll(int page, int size, String status, String searchKey);
    List<FoodOrder> findByUserId(Long userId);
    List<FoodOrder> findByUserId(Long userId, int page, int size);
    FoodOrder findById(Long id);
    FoodOrder findByOrderNo(String orderNo);
    Map<String, Object> create(FoodOrder order);
    boolean pay(String orderNo);
    boolean cancel(String orderNo);
    boolean use(String orderNo);
    boolean complete(String orderNo);
    boolean update(FoodOrder order);
    boolean deleteById(Long id);
    Map<String, Object> getStats();
    int count();
    int count(String status, String searchKey);
    int countByUserId(Long userId);
}
