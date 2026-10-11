package org.example.sanjiang.service;

import org.example.sanjiang.entity.Order;
import java.util.Map;

public interface WechatPayService {
    /**
     * 创建微信支付订单
     */
    Map<String, Object> createPayOrder(Order order);

    /**
     * 处理微信支付回调
     */
    boolean handlePayCallback(Map<String, Object> callbackData);

    /**
     * 申请退款
     */
    boolean refund(String orderNo, String reason);

    /**
     * 查询支付状态
     */
    Map<String, Object> queryPayStatus(String orderNo);
}
