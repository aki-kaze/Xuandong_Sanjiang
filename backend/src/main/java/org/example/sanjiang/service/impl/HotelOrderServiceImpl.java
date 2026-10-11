package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.HotelOrder;
import org.example.sanjiang.entity.User;
import org.example.sanjiang.mapper.HotelOrderMapper;
import org.example.sanjiang.mapper.UserMapper;
import org.example.sanjiang.service.HotelOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotelOrderServiceImpl implements HotelOrderService {

    @Autowired
    private HotelOrderMapper hotelOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<HotelOrder> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return hotelOrderMapper.findAll();
    }

    @Override
    public List<HotelOrder> findAll(int page, int size, String status, String searchKey) {
        PageHelper.startPage(page, size);
        return hotelOrderMapper.findByCondition(status, searchKey);
    }

    @Override
    public List<HotelOrder> findByUserId(Long userId) {
        return hotelOrderMapper.findByUserId(userId);
    }

    @Override
    public List<HotelOrder> findByUserId(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        return hotelOrderMapper.findByUserId(userId);
    }

    @Override
    public HotelOrder findById(Long id) {
        return hotelOrderMapper.findById(id);
    }

    @Override
    public HotelOrder findByOrderNo(String orderNo) {
        return hotelOrderMapper.findByOrderNo(orderNo);
    }

    @Override
    public Map<String, Object> create(HotelOrder order) {
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo("HT" + System.currentTimeMillis());
        }
        order.setStatus("pending");
        order.setCreatedAt(java.time.LocalDateTime.now());
        order.setUpdatedAt(java.time.LocalDateTime.now());
        if (order.getQuantity() == null) order.setQuantity(1);
        if (order.getTotalPrice() == null && order.getPrice() != null && order.getQuantity() != null) {
            order.setTotalPrice(order.getPrice().multiply(java.math.BigDecimal.valueOf(order.getQuantity())));
        }

        // 自动获取用户的 name 和 phone
        if (order.getUserId() != null && order.getUserId() > 0) {
            User user = userMapper.findById(order.getUserId().intValue());
            if (user != null) {
                if (order.getContactName() == null || order.getContactName().isEmpty()) {
                    order.setContactName(user.getName());
                }
                if (order.getContactPhone() == null || order.getContactPhone().isEmpty()) {
                    order.setContactPhone(user.getPhone());
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        if (hotelOrderMapper.insert(order) > 0) {
            result.put("success", true);
            result.put("order", order);
        } else {
            result.put("success", false);
        }
        return result;
    }

    @Override
    public boolean pay(String orderNo) {
        HotelOrder order = hotelOrderMapper.findByOrderNo(orderNo);
        if (order != null && "pending".equals(order.getStatus())) {
            order.setStatus("paid");
            order.setPaidAt(java.time.LocalDateTime.now());
            return hotelOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean cancel(String orderNo) {
        HotelOrder order = hotelOrderMapper.findByOrderNo(orderNo);
        if (order == null) {
            try { order = hotelOrderMapper.findById(Long.parseLong(orderNo)); } catch (Exception e) {}
        }
        if (order != null && ("pending".equals(order.getStatus()) || "paid".equals(order.getStatus()))) {
            order.setStatus("cancelled");
            return hotelOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean use(String orderNo) {
        HotelOrder order = hotelOrderMapper.findByOrderNo(orderNo);
        if (order != null && "paid".equals(order.getStatus())) {
            order.setStatus("checked_in");
            return hotelOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean checkIn(String orderNo) {
        HotelOrder order = hotelOrderMapper.findByOrderNo(orderNo);
        if (order != null && "paid".equals(order.getStatus())) {
            order.setStatus("checked_in");
            return hotelOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean checkOut(String orderNo) {
        HotelOrder order = hotelOrderMapper.findByOrderNo(orderNo);
        if (order != null && "checked_in".equals(order.getStatus())) {
            order.setStatus("checked_out");
            return hotelOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean update(HotelOrder order) {
        return hotelOrderMapper.update(order) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return hotelOrderMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", hotelOrderMapper.countAll());
        stats.put("pendingOrders", hotelOrderMapper.countByStatus("pending"));
        stats.put("paidOrders", hotelOrderMapper.countByStatus("paid"));
        stats.put("checkedInOrders", hotelOrderMapper.countByStatus("checked_in"));
        stats.put("checkedOutOrders", hotelOrderMapper.countByStatus("checked_out"));
        stats.put("cancelledOrders", hotelOrderMapper.countByStatus("cancelled"));
        stats.put("totalRevenue", hotelOrderMapper.sumRevenue());
        return stats;
    }

    @Override
    public int count() {
        return hotelOrderMapper.countAll();
    }

    @Override
    public int countByUserId(Long userId) {
        return hotelOrderMapper.countByUserId(userId);
    }

    @Override
    public int count(String status, String searchKey) {
        return hotelOrderMapper.countByCondition(status, searchKey);
    }
}
