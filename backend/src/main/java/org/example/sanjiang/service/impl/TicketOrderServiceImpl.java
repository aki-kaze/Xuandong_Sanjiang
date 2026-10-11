package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.TicketOrder;
import org.example.sanjiang.entity.User;
import org.example.sanjiang.mapper.TicketOrderMapper;
import org.example.sanjiang.mapper.UserMapper;
import org.example.sanjiang.service.TicketOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketOrderServiceImpl implements TicketOrderService {

    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<TicketOrder> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return ticketOrderMapper.findAll();
    }

    @Override
    public List<TicketOrder> findAll(int page, int size, String status, String searchKey) {
        PageHelper.startPage(page, size);
        return ticketOrderMapper.findByCondition(status, searchKey);
    }

    @Override
    public List<TicketOrder> findByUserId(Long userId) {
        return ticketOrderMapper.findByUserId(userId);
    }

    @Override
    public List<TicketOrder> findByUserId(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        return ticketOrderMapper.findByUserId(userId);
    }

    @Override
    public TicketOrder findById(Long id) {
        return ticketOrderMapper.findById(id);
    }

    @Override
    public TicketOrder findByOrderNo(String orderNo) {
        return ticketOrderMapper.findByOrderNo(orderNo);
    }

    @Override
    public Map<String, Object> create(TicketOrder order) {
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo("TK" + System.currentTimeMillis());
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
        if (ticketOrderMapper.insert(order) > 0) {
            result.put("success", true);
            result.put("order", order);
        } else {
            result.put("success", false);
        }
        return result;
    }

    @Override
    public boolean pay(String orderNo) {
        TicketOrder order = ticketOrderMapper.findByOrderNo(orderNo);
        if (order != null && "pending".equals(order.getStatus())) {
            order.setStatus("paid");
            order.setPaidAt(java.time.LocalDateTime.now());
            return ticketOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean cancel(String orderNo) {
        TicketOrder order = ticketOrderMapper.findByOrderNo(orderNo);
        if (order == null) {
            try { order = ticketOrderMapper.findById(Long.parseLong(orderNo)); } catch (Exception e) {}
        }
        if (order != null && ("pending".equals(order.getStatus()) || "paid".equals(order.getStatus()))) {
            order.setStatus("cancelled");
            return ticketOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean use(String orderNo) {
        TicketOrder order = ticketOrderMapper.findByOrderNo(orderNo);
        if (order != null && "paid".equals(order.getStatus())) {
            order.setStatus("used");
            return ticketOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean update(TicketOrder order) {
        return ticketOrderMapper.update(order) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return ticketOrderMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", ticketOrderMapper.countAll());
        stats.put("pendingOrders", ticketOrderMapper.countByStatus("pending"));
        stats.put("paidOrders", ticketOrderMapper.countByStatus("paid"));
        stats.put("completedOrders", ticketOrderMapper.countByStatus("used"));
        stats.put("cancelledOrders", ticketOrderMapper.countByStatus("cancelled"));
        stats.put("totalRevenue", ticketOrderMapper.sumRevenue());
        return stats;
    }

    @Override
    public int count() {
        return ticketOrderMapper.countAll();
    }

    @Override
    public int count(String status, String searchKey) {
        return ticketOrderMapper.countByCondition(status, searchKey);
    }

    @Override
    public int countByUserId(Long userId) {
        return ticketOrderMapper.countByUserId(userId);
    }
}
