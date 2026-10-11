package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.Order;
import org.example.sanjiang.entity.User;
import org.example.sanjiang.mapper.OrderMapper;
import org.example.sanjiang.mapper.UserMapper;
import org.example.sanjiang.service.OrderService;
import org.example.sanjiang.service.WechatPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired(required = false)
    private WechatPayService wechatPayService;

    @Override
    public List<Order> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return orderMapper.findAll();
    }

    @Override
    public List<Order> findByUserId(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        return orderMapper.findByUserId(userId);
    }

    @Override
    public Order findById(Long id) {
        return orderMapper.findById(id);
    }

    @Override
    public Order findByOrderNo(String orderNo) {
        return orderMapper.findByOrderNo(orderNo);
    }

    @Override
    public Map<String, Object> create(Order order) {
        order.setOrderNo("ORD" + System.currentTimeMillis());
        order.setStatus("pending");
        order.setVerifyCode(generateVerifyCode());
        if (order.getType() == null) order.setType("ticket");
        if (order.getItemId() == null) order.setItemId(0L);
        if (order.getItemName() == null) order.setItemName("");
        if (order.getTotalPrice() == null && order.getPrice() != null && order.getQuantity() != null) {
            order.setTotalPrice(order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
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
        if (orderMapper.insert(order) > 0) {
            result.put("success", true);
            result.put("order", order);
        } else {
            result.put("success", false);
        }
        return result;
    }

    @Override
    public boolean pay(String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order != null && "pending".equals(order.getStatus())) {
            order.setStatus("paid");
            order.setPaidAt(LocalDateTime.now());
            order.setPayType("wechat");
            return orderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean cancel(String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            try { order = orderMapper.findById(Long.parseLong(orderNo)); } catch (Exception e) {}
        }
        if (order != null) {
            // 状态机：只有 pending 和 paid 状态可以取消
            if ("pending".equals(order.getStatus()) || "paid".equals(order.getStatus())) {
                order.setStatus("cancelled");
                return orderMapper.update(order) > 0;
            }
        }
        return false;
    }

    @Override
    public boolean use(String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order != null && "paid".equals(order.getStatus())) {
            order.setStatus("used");
            order.setVerifyTime(LocalDateTime.now());
            return orderMapper.update(order) > 0;
        }
        return false;
    }

    /**
     * 核销码核销
     */
    public boolean verifyByCode(String verifyCode) {
        Order order = orderMapper.findByVerifyCode(verifyCode);
        if (order != null && "paid".equals(order.getStatus())) {
            order.setStatus("used");
            order.setVerifyTime(LocalDateTime.now());
            return orderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean deleteById(Long id) {
        return orderMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orderMapper.countAll());
        stats.put("pendingOrders", orderMapper.countByStatus("pending"));
        stats.put("paidOrders", orderMapper.countByStatus("paid"));
        stats.put("completedOrders", orderMapper.countByStatus("used") + orderMapper.countByStatus("completed"));
        stats.put("cancelledOrders", orderMapper.countByStatus("cancelled"));
        stats.put("refundedOrders", orderMapper.countByStatus("refunded"));

        // 计算转化率
        int totalOrders = orderMapper.countAll();
        int paidOrders = orderMapper.countByStatus("paid") + orderMapper.countByStatus("used") + orderMapper.countByStatus("completed");
        stats.put("conversionRate", totalOrders > 0 ? BigDecimal.valueOf(paidOrders * 100.0 / totalOrders).setScale(2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);

        return stats;
    }

    @Override
    public int count() {
        return orderMapper.countAll();
    }

    @Override
    public int countByUserId(Long userId) {
        return orderMapper.countByUserId(userId);
    }

    /**
     * 生成6位数字核销码
     */
    private String generateVerifyCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
