package org.example.sanjiang.service;

import org.example.sanjiang.entity.TicketOrder;
import java.util.List;
import java.util.Map;

public interface TicketOrderService {
    List<TicketOrder> findAll(int page, int size);
    List<TicketOrder> findAll(int page, int size, String status, String searchKey);
    List<TicketOrder> findByUserId(Long userId);
    List<TicketOrder> findByUserId(Long userId, int page, int size);
    TicketOrder findById(Long id);
    TicketOrder findByOrderNo(String orderNo);
    Map<String, Object> create(TicketOrder order);
    boolean pay(String orderNo);
    boolean cancel(String orderNo);
    boolean use(String orderNo);
    boolean update(TicketOrder order);
    boolean deleteById(Long id);
    Map<String, Object> getStats();
    int count();
    int count(String status, String searchKey);
    int countByUserId(Long userId);
}
