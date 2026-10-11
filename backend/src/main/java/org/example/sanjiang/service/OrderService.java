package org.example.sanjiang.service;

import org.example.sanjiang.entity.Order;
import java.util.List;
import java.util.Map;

public interface OrderService {
    List<Order> findAll(int page, int size);
    List<Order> findByUserId(Long userId, int page, int size);
    Order findById(Long id);
    Order findByOrderNo(String orderNo);
    Map<String, Object> create(Order order);
    boolean pay(String orderNo);
    boolean cancel(String orderNo);
    boolean use(String orderNo);
    boolean deleteById(Long id);
    Map<String, Object> getStats();
    int count();
    int countByUserId(Long userId);
}
