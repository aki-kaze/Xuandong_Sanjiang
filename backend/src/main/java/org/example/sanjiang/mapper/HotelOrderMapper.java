package org.example.sanjiang.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.sanjiang.entity.HotelOrder;
import java.util.List;

public interface HotelOrderMapper {
    List<HotelOrder> findAll();
    List<HotelOrder> findByCondition(@Param("status") String status, @Param("searchKey") String searchKey);
    List<HotelOrder> findByUserId(Long userId);
    List<HotelOrder> findByStatus(String status);
    HotelOrder findById(Long id);
    HotelOrder findByOrderNo(String orderNo);
    int insert(HotelOrder order);
    int update(HotelOrder order);
    int deleteById(Long id);
    int countAll();
    int countByUserId(Long userId);
    int countByStatus(String status);
    int countByCondition(@Param("status") String status, @Param("searchKey") String searchKey);
    int countToday();
    int countTodayByStatus(String status);
    java.math.BigDecimal sumTotalPriceByStatus(java.util.List<String> statuses);
    java.math.BigDecimal sumRevenue();
    List<org.example.sanjiang.entity.HotelOrder> findRecent(int limit);
}
