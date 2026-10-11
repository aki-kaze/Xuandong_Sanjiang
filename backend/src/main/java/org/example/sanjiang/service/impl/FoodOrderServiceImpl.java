package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.FoodOrder;
import org.example.sanjiang.entity.User;
import org.example.sanjiang.mapper.FoodOrderMapper;
import org.example.sanjiang.mapper.UserMapper;
import org.example.sanjiang.service.FoodOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FoodOrderServiceImpl implements FoodOrderService {

    @Autowired
    private FoodOrderMapper foodOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<FoodOrder> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return foodOrderMapper.findAll();
    }

    @Override
    public List<FoodOrder> findAll(int page, int size, String status, String searchKey) {
        PageHelper.startPage(page, size);
        return foodOrderMapper.findByCondition(status, searchKey);
    }

    @Override
    public List<FoodOrder> findByUserId(Long userId) {
        return foodOrderMapper.findByUserId(userId);
    }

    @Override
    public List<FoodOrder> findByUserId(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        return foodOrderMapper.findByUserId(userId);
    }

    @Override
    public FoodOrder findById(Long id) {
        return foodOrderMapper.findById(id);
    }

    @Override
    public FoodOrder findByOrderNo(String orderNo) {
        return foodOrderMapper.findByOrderNo(orderNo);
    }

    @Override
    public Map<String, Object> create(FoodOrder order) {
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo("FD" + System.currentTimeMillis());
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
        if (foodOrderMapper.insert(order) > 0) {
            result.put("success", true);
            result.put("order", order);
        } else {
            result.put("success", false);
        }
        return result;
    }

    @Override
    public boolean pay(String orderNo) {
        FoodOrder order = foodOrderMapper.findByOrderNo(orderNo);
        if (order != null && "pending".equals(order.getStatus())) {
            order.setStatus("paid");
            order.setPaidAt(java.time.LocalDateTime.now());
            return foodOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean cancel(String orderNo) {
        FoodOrder order = foodOrderMapper.findByOrderNo(orderNo);
        if (order == null) {
            try { order = foodOrderMapper.findById(Long.parseLong(orderNo)); } catch (Exception e) {}
        }
        if (order != null && ("pending".equals(order.getStatus()) || "paid".equals(order.getStatus()))) {
            order.setStatus("cancelled");
            return foodOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean use(String orderNo) {
        FoodOrder order = foodOrderMapper.findByOrderNo(orderNo);
        if (order != null && "paid".equals(order.getStatus())) {
            order.setStatus("used");
            return foodOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean complete(String orderNo) {
        FoodOrder order = foodOrderMapper.findByOrderNo(orderNo);
        if (order != null && "paid".equals(order.getStatus())) {
            order.setStatus("completed");
            return foodOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean update(FoodOrder order) {
        return foodOrderMapper.update(order) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return foodOrderMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", foodOrderMapper.countAll());
        stats.put("pendingOrders", foodOrderMapper.countByStatus("pending"));
        stats.put("paidOrders", foodOrderMapper.countByStatus("paid"));
        stats.put("completedOrders", foodOrderMapper.countByStatus("used"));
        stats.put("cancelledOrders", foodOrderMapper.countByStatus("cancelled"));
        stats.put("totalRevenue", foodOrderMapper.sumRevenue());
        return stats;
    }

    @Override
    public int count() {
        return foodOrderMapper.countAll();
    }

    @Override
    public int count(String status, String searchKey) {
        return foodOrderMapper.countByCondition(status, searchKey);
    }

    @Override
    public int countByUserId(Long userId) {
        return foodOrderMapper.countByUserId(userId);
    }
}
