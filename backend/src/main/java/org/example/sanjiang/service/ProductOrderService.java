package org.example.sanjiang.service;

import org.example.sanjiang.entity.ProductOrder;
import java.util.List;
import java.util.Map;

public interface ProductOrderService {
    List<ProductOrder> findAll(int page, int size);
    List<ProductOrder> findAll(int page, int size, String status, String searchKey);
    List<ProductOrder> findByUserId(Long userId);
    List<ProductOrder> findByUserId(Long userId, int page, int size);
    ProductOrder findById(Long id);
    ProductOrder findByOrderNo(String orderNo);
    Map<String, Object> create(ProductOrder order);
    boolean pay(String orderNo);
    boolean cancel(String orderNo);
    boolean use(String orderNo);
    boolean ship(String orderNo);
    boolean deliver(String orderNo);
    boolean update(ProductOrder order);
    boolean deleteById(Long id);
    Map<String, Object> getStats();
    int count();
    int count(String status, String searchKey);
    int countByUserId(Long userId);
}
