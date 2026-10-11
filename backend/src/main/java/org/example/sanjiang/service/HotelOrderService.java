package org.example.sanjiang.service;

import org.example.sanjiang.entity.HotelOrder;
import java.util.List;
import java.util.Map;

public interface HotelOrderService {
    List<HotelOrder> findAll(int page, int size);
    List<HotelOrder> findAll(int page, int size, String status, String searchKey);
    List<HotelOrder> findByUserId(Long userId);
    List<HotelOrder> findByUserId(Long userId, int page, int size);
    HotelOrder findById(Long id);
    HotelOrder findByOrderNo(String orderNo);
    Map<String, Object> create(HotelOrder order);
    boolean pay(String orderNo);
    boolean cancel(String orderNo);
    boolean use(String orderNo);
    boolean checkIn(String orderNo);
    boolean checkOut(String orderNo);
    boolean update(HotelOrder order);
    boolean deleteById(Long id);
    Map<String, Object> getStats();
    int count();
    int countByUserId(Long userId);
    int count(String status, String searchKey);
}
