package org.example.sanjiang.service.impl;

import org.example.sanjiang.entity.Order;
import org.example.sanjiang.service.WechatPayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WechatPayServiceImpl implements WechatPayService {

    private static final Logger log = LoggerFactory.getLogger(WechatPayServiceImpl.class);

    @Value("${wechat.pay.app-id:}")
    private String appId;

    @Value("${wechat.pay.mch-id:}")
    private String mchId;

    @Value("${wechat.pay.api-key:}")
    private String apiKey;

    @Value("${wechat.pay.notify-url:}")
    private String notifyUrl;

    @Override
    public Map<String, Object> createPayOrder(Order order) {
        // 微信支付统一下单接口
        // 实际项目中需要调用微信支付API，这里返回预支付信息结构
        Map<String, Object> payParams = new HashMap<>();
        String nonceStr = UUID.randomUUID().toString().replace("-", "");

        payParams.put("appId", appId);
        payParams.put("mchId", mchId);
        payParams.put("nonceStr", nonceStr);
        payParams.put("body", order.getItemName() + " - 寻侗三江");
        payParams.put("outTradeNo", order.getOrderNo());
        payParams.put("totalFee", order.getTotalPrice().multiply(new BigDecimal("100")).intValue());
        payParams.put("notifyUrl", notifyUrl);
        payParams.put("tradeType", "JSAPI"); // 小程序支付

        // 生成签名（实际项目中需要实现签名算法）
        payParams.put("sign", generateSign(payParams));

        log.info("创建微信支付订单: orderNo={}, totalFee={}", order.getOrderNo(), payParams.get("totalFee"));
        return payParams;
    }

    @Override
    public boolean handlePayCallback(Map<String, Object> callbackData) {
        // 验证签名
        String sign = (String) callbackData.get("sign");
        if (!verifySign(callbackData, sign)) {
            log.error("微信支付回调签名验证失败");
            return false;
        }

        String orderNo = (String) callbackData.get("out_trade_no");
        String resultCode = (String) callbackData.get("result_code");

        log.info("收到微信支付回调: orderNo={}, resultCode={}", orderNo, resultCode);

        // SUCCESS 表示支付成功
        return "SUCCESS".equals(resultCode);
    }

    @Override
    public boolean refund(String orderNo, String reason) {
        // 微信支付退款接口
        // 实际项目中需要调用微信支付退款API
        log.info("申请退款: orderNo={}, reason={}", orderNo, reason);
        return true;
    }

    @Override
    public Map<String, Object> queryPayStatus(String orderNo) {
        // 微信支付查询接口
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("tradeState", "SUCCESS"); // 实际项目中需要调用微信支付查询API
        return result;
    }

    private String generateSign(Map<String, Object> params) {
        // 微信支付签名算法（MD5/HMAC-SHA256）
        // 实际项目中需要完整实现
        return UUID.randomUUID().toString().replace("-", "");
    }

    private boolean verifySign(Map<String, Object> params, String sign) {
        // 验证微信支付回调签名
        // 实际项目中需要完整实现
        return true;
    }
}
