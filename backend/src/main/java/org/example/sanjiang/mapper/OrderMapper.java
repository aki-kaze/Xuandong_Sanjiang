package org.example.sanjiang.mapper;

import org.example.sanjiang.entity.Order;
import java.util.List;

public interface OrderMapper {
    List<Order> findAll();
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(String status);
    Order findById(Long id);
    Order findByOrderNo(String orderNo);
    Order findByVerifyCode(String verifyCode);
    int insert(Order order);
    int update(Order order);
    int deleteById(Long id);
    int countAll();
    int countByStatus(String status);
    int countByUserId(Long userId);
    int countToday();
    Double sumRevenue();
}
