package org.example.sanjiang.mapper;

import org.example.sanjiang.entity.TicketOrder;
import java.util.List;

public interface TicketOrderMapper {
    List<TicketOrder> findAll();
    List<TicketOrder> findByCondition(String status, String searchKey);
    List<TicketOrder> findByUserId(Long userId);
    List<TicketOrder> findByStatus(String status);
    TicketOrder findById(Long id);
    TicketOrder findByOrderNo(String orderNo);
    int insert(TicketOrder order);
    int update(TicketOrder order);
    int deleteById(Long id);
    int countAll();
    int countByCondition(String status, String searchKey);
    int countByUserId(Long userId);
    int countByStatus(String status);
    int countToday();
    int countTodayByStatus(String status);
    java.math.BigDecimal sumTotalPriceByStatus(java.util.List<String> statuses);
    java.math.BigDecimal sumRevenue();
    List<org.example.sanjiang.entity.TicketOrder> findRecent(int limit);
}
