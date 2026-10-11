package org.example.sanjiang.mapper;

import org.example.sanjiang.entity.ProductOrder;
import java.util.List;

public interface ProductOrderMapper {
    List<ProductOrder> findAll();
    List<ProductOrder> findByCondition(String status, String searchKey);
    List<ProductOrder> findByUserId(Long userId);
    List<ProductOrder> findByStatus(String status);
    ProductOrder findById(Long id);
    ProductOrder findByOrderNo(String orderNo);
    int insert(ProductOrder order);
    int update(ProductOrder order);
    int deleteById(Long id);
    int countAll();
    int countByCondition(String status, String searchKey);
    int countByUserId(Long userId);
    int countByStatus(String status);
    int countToday();
    int countTodayByStatus(String status);
    java.math.BigDecimal sumTotalPriceByStatus(java.util.List<String> statuses);
    java.math.BigDecimal sumRevenue();
    List<org.example.sanjiang.entity.ProductOrder> findRecent(int limit);
}
