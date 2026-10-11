package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.ProductOrder;
import org.example.sanjiang.entity.User;
import org.example.sanjiang.mapper.ProductOrderMapper;
import org.example.sanjiang.mapper.UserMapper;
import org.example.sanjiang.service.ProductOrderService;
import org.example.sanjiang.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductOrderServiceImpl implements ProductOrderService {

    @Autowired
    private ProductOrderMapper productOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductService productService;

    @Override
    public List<ProductOrder> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return productOrderMapper.findAll();
    }

    @Override
    public List<ProductOrder> findAll(int page, int size, String status, String searchKey) {
        PageHelper.startPage(page, size);
        return productOrderMapper.findByCondition(status, searchKey);
    }

    @Override
    public List<ProductOrder> findByUserId(Long userId) {
        return productOrderMapper.findByUserId(userId);
    }

    @Override
    public List<ProductOrder> findByUserId(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        return productOrderMapper.findByUserId(userId);
    }

    @Override
    public ProductOrder findById(Long id) {
        return productOrderMapper.findById(id);
    }

    @Override
    public ProductOrder findByOrderNo(String orderNo) {
        return productOrderMapper.findByOrderNo(orderNo);
    }

    @Override
    public Map<String, Object> create(ProductOrder order) {
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo("PD" + System.currentTimeMillis());
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
        if (productOrderMapper.insert(order) > 0) {
            result.put("success", true);
            result.put("order", order);
        } else {
            result.put("success", false);
        }
        return result;
    }

    @Override
    public boolean pay(String orderNo) {
        ProductOrder order = productOrderMapper.findByOrderNo(orderNo);
        if (order != null && "pending".equals(order.getStatus())) {
            order.setStatus("paid");
            order.setPaidAt(java.time.LocalDateTime.now());
            boolean updated = productOrderMapper.update(order) > 0;
            if (updated && order.getProductId() != null && order.getQuantity() != null) {
                productService.increaseSales(order.getProductId().intValue(), order.getQuantity());
            }
            return updated;
        }
        return false;
    }

    @Override
    public boolean cancel(String orderNo) {
        ProductOrder order = productOrderMapper.findByOrderNo(orderNo);
        if (order == null) {
            try { order = productOrderMapper.findById(Long.parseLong(orderNo)); } catch (Exception e) {}
        }
        if (order != null && ("pending".equals(order.getStatus()) || "paid".equals(order.getStatus()))) {
            order.setStatus("cancelled");
            return productOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean use(String orderNo) {
        ProductOrder order = productOrderMapper.findByOrderNo(orderNo);
        if (order != null && "paid".equals(order.getStatus())) {
            order.setStatus("used");
            return productOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean ship(String orderNo) {
        ProductOrder order = productOrderMapper.findByOrderNo(orderNo);
        if (order != null && "paid".equals(order.getStatus())) {
            order.setStatus("shipped");
            return productOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean deliver(String orderNo) {
        ProductOrder order = productOrderMapper.findByOrderNo(orderNo);
        if (order != null && "shipped".equals(order.getStatus())) {
            order.setStatus("delivered");
            return productOrderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public boolean update(ProductOrder order) {
        return productOrderMapper.update(order) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return productOrderMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", productOrderMapper.countAll());
        stats.put("pendingOrders", productOrderMapper.countByStatus("pending"));
        stats.put("paidOrders", productOrderMapper.countByStatus("paid"));
        stats.put("shippedOrders", productOrderMapper.countByStatus("shipped"));
        stats.put("deliveredOrders", productOrderMapper.countByStatus("delivered"));
        stats.put("cancelledOrders", productOrderMapper.countByStatus("cancelled"));
        stats.put("totalRevenue", productOrderMapper.sumRevenue());
        return stats;
    }

    @Override
    public int count() {
        return productOrderMapper.countAll();
    }

    @Override
    public int count(String status, String searchKey) {
        return productOrderMapper.countByCondition(status, searchKey);
    }

    @Override
    public int countByUserId(Long userId) {
        return productOrderMapper.countByUserId(userId);
    }
}
