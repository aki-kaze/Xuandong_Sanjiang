package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.service.OrderService;
import org.example.sanjiang.service.impl.OrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class WechatPayController {

    @Autowired
    private OrderService orderService;

    /**
     * 微信支付回调
     */
    @PostMapping("/wechat/callback")
    public Result<String> wechatPayCallback(@RequestBody Map<String, Object> callbackData) {
        try {
            String orderNo = (String) callbackData.get("out_trade_no");
            // 支付成功，更新订单状态
            boolean success = orderService.pay(orderNo);
            if (success) {
                return Result.success("SUCCESS");
            }
            return Result.error("FAIL");
        } catch (Exception e) {
            return Result.error("FAIL");
        }
    }

    /**
     * 核销码核销
     */
    @PostMapping("/verify")
    public Result<Map<String, Object>> verifyByCode(@RequestBody Map<String, String> body) {
        String verifyCode = body.get("verifyCode");
        if (verifyCode == null || verifyCode.isEmpty()) {
            return Result.error("核销码不能为空");
        }

        if (orderService instanceof OrderServiceImpl) {
            boolean success = ((OrderServiceImpl) orderService).verifyByCode(verifyCode);
            Map<String, Object> result = new HashMap<>();
            if (success) {
                result.put("success", true);
                result.put("message", "核销成功");
                return Result.success(result);
            }
            result.put("success", false);
            result.put("message", "核销码无效或订单状态异常");
            return Result.success(result);
        }
        return Result.error("核销服务不可用");
    }
}
