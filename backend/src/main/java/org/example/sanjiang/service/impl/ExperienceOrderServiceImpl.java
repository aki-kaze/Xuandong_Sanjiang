package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.ExperienceOrder;
import org.example.sanjiang.entity.User;
import org.example.sanjiang.mapper.ExperienceOrderMapper;
import org.example.sanjiang.mapper.UserMapper;
import org.example.sanjiang.service.ExperienceOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExperienceOrderServiceImpl implements ExperienceOrderService {

    @Autowired
    private ExperienceOrderMapper experienceOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<ExperienceOrder> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return experienceOrderMapper.findAll();
    }

    @Override
    public List<ExperienceOrder> findByCondition(String status, String searchKey, int page, int size) {
        PageHelper.startPage(page, size);
        return experienceOrderMapper.findByCondition(status, searchKey);
    }

    @Override
    public List<ExperienceOrder> findByUserId(Long userId) {
        return experienceOrderMapper.findByUserId(userId);
    }

    @Override
    public List<ExperienceOrder> findByUserId(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        return experienceOrderMapper.findByUserId(userId);
    }

    @Override
    public ExperienceOrder findById(Long id) {
        return experienceOrderMapper.findById(id);
    }

    @Override
    public ExperienceOrder findByOrderNo(String orderNo) {
        return experienceOrderMapper.findByOrderNo(orderNo);
    }

    @Override
    public Map<String, Object> create(ExperienceOrder order) {
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo("EX" + System.currentTimeMillis());
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
        if (experienceOrderMapper.insert(order) > 0) {
            result.put("success", true);
            result.put("order", order);
        } else {
            result.put("success", false);
        }
        return result;
    }

    @Override
    public boolean confirm(String orderNo) {
        ExperienceOrder order = experienceOrderMapper.findByOrderNo(orderNo);
        if (order != null && "pending".equals(order.getStatus())) {
            order.setStatus("confirmed");
            order.setConfirmedAt(java.time.LocalDateTime.now());
            return experienceOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean complete(String orderNo) {
        ExperienceOrder order = experienceOrderMapper.findByOrderNo(orderNo);
        if (order != null && "confirmed".equals(order.getStatus())) {
            order.setStatus("completed");
            order.setCompletedAt(java.time.LocalDateTime.now());
            return experienceOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean cancel(String orderNo, String reason) {
        ExperienceOrder order = experienceOrderMapper.findByOrderNo(orderNo);
        if (order == null) {
            try { order = experienceOrderMapper.findById(Long.parseLong(orderNo)); } catch (Exception e) {}
        }
        if (order != null && ("pending".equals(order.getStatus()) || "confirmed".equals(order.getStatus()))) {
            order.setStatus("cancelled");
            order.setCancelReason(reason);
            return experienceOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean update(ExperienceOrder order) {
        return experienceOrderMapper.update(order) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return experienceOrderMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", experienceOrderMapper.countAll());
        stats.put("pendingOrders", experienceOrderMapper.countByStatus("pending"));
        stats.put("confirmedOrders", experienceOrderMapper.countByStatus("confirmed"));
        stats.put("completedOrders", experienceOrderMapper.countByStatus("completed"));
        stats.put("cancelledOrders", experienceOrderMapper.countByStatus("cancelled"));
        stats.put("totalRevenue", experienceOrderMapper.sumRevenue());
        return stats;
    }

    @Override
    public int count() {
        return experienceOrderMapper.countAll();
    }

    @Override
    public int countByCondition(String status, String searchKey) {
        return experienceOrderMapper.countByCondition(status, searchKey);
    }

    @Override
    public int countByUserId(Long userId) {
        return experienceOrderMapper.countByUserId(userId);
    }
}
