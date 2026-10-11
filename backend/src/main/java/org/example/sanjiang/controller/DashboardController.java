package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.*;
import org.example.sanjiang.service.*;
import org.example.sanjiang.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin
public class DashboardController {

    @Autowired
    private AttractionService attractionService;

    @Autowired
    private HeritageService heritageService;

    @Autowired
    private UserService userService;

    @Autowired
    private MomentService momentService;

    @Autowired
    private PostService postService;

    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    @Autowired
    private FoodOrderMapper foodOrderMapper;

    @Autowired
    private HotelOrderMapper hotelOrderMapper;

    @Autowired
    private ProductOrderMapper productOrderMapper;

    @Autowired
    private ExperienceOrderMapper experienceOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 基础计数
        stats.put("attractions", attractionService.count());
        stats.put("heritages", heritageService.count());
        stats.put("users", userService.count());
        stats.put("posts", momentService.count());
        stats.put("pendingPosts", momentService.countByStatus(0));

        // 总订单数
        int totalOrders = ticketOrderMapper.countAll() +
                          foodOrderMapper.countAll() +
                          hotelOrderMapper.countAll() +
                          productOrderMapper.countAll() +
                          experienceOrderMapper.countAll();
        stats.put("orders", totalOrders);

        // 今日订单数
        int todayOrders = ticketOrderMapper.countToday() +
                          foodOrderMapper.countToday() +
                          hotelOrderMapper.countToday() +
                          productOrderMapper.countToday() +
                          experienceOrderMapper.countToday();
        stats.put("todayOrders", todayOrders);

        // 今日新增用户
        stats.put("todayUsers", userMapper.countToday());

        // 订单状态分布
        Map<String, Object> translatedOrderStats = new LinkedHashMap<>();
        int pendingOrders = countByStatus("pending");
        int paidOrders = countByStatus("paid");
        int completedOrders = countByStatus("used") + countByStatus("completed");
        int cancelledOrders = countByStatus("cancelled");
        translatedOrderStats.put("待支付", pendingOrders);
        translatedOrderStats.put("待使用", paidOrders);
        translatedOrderStats.put("已完成", completedOrders);
        translatedOrderStats.put("已取消", cancelledOrders);
        stats.put("orderStats", translatedOrderStats);

        // 各类订单收入
        BigDecimal ticketRevenue = Optional.ofNullable(ticketOrderMapper.sumRevenue()).orElse(BigDecimal.ZERO);
        BigDecimal foodRevenue = Optional.ofNullable(foodOrderMapper.sumRevenue()).orElse(BigDecimal.ZERO);
        BigDecimal hotelRevenue = Optional.ofNullable(hotelOrderMapper.sumRevenue()).orElse(BigDecimal.ZERO);
        BigDecimal productRevenue = Optional.ofNullable(productOrderMapper.sumRevenue()).orElse(BigDecimal.ZERO);
        BigDecimal experienceRevenue = Optional.ofNullable(experienceOrderMapper.sumRevenue()).orElse(BigDecimal.ZERO);
        BigDecimal totalRevenue = ticketRevenue.add(foodRevenue).add(hotelRevenue).add(productRevenue).add(experienceRevenue);

        stats.put("totalRevenue", totalRevenue);
        stats.put("ticketRevenue", ticketRevenue);
        stats.put("foodRevenue", foodRevenue);
        stats.put("hotelRevenue", hotelRevenue);
        stats.put("productRevenue", productRevenue);
        stats.put("experienceRevenue", experienceRevenue);

        // 转化率分析
        Map<String, Object> conversionStats = new LinkedHashMap<>();

        int ticketTotal = ticketOrderMapper.countAll();
        int ticketPaid = ticketOrderMapper.countByStatus("paid") + ticketOrderMapper.countByStatus("used") + ticketOrderMapper.countByStatus("completed");
        conversionStats.put("ticketConversionRate", calcRate(ticketPaid, ticketTotal));
        conversionStats.put("ticketOrders", ticketTotal);
        conversionStats.put("ticketPaidOrders", ticketPaid);

        int foodTotal = foodOrderMapper.countAll();
        int foodPaid = foodOrderMapper.countByStatus("paid") + foodOrderMapper.countByStatus("used") + foodOrderMapper.countByStatus("completed");
        conversionStats.put("foodConversionRate", calcRate(foodPaid, foodTotal));
        conversionStats.put("foodOrders", foodTotal);
        conversionStats.put("foodPaidOrders", foodPaid);

        int hotelTotal = hotelOrderMapper.countAll();
        int hotelPaid = hotelOrderMapper.countByStatus("paid") + hotelOrderMapper.countByStatus("checked_in");
        conversionStats.put("hotelConversionRate", calcRate(hotelPaid, hotelTotal));
        conversionStats.put("hotelOrders", hotelTotal);
        conversionStats.put("hotelPaidOrders", hotelPaid);

        int productTotal = productOrderMapper.countAll();
        int productPaid = productOrderMapper.countByStatus("paid") + productOrderMapper.countByStatus("used") + productOrderMapper.countByStatus("completed");
        conversionStats.put("productConversionRate", calcRate(productPaid, productTotal));
        conversionStats.put("productOrders", productTotal);
        conversionStats.put("productPaidOrders", productPaid);

        int experienceTotal = experienceOrderMapper.countAll();
        int experiencePaid = experienceOrderMapper.countByStatus("confirmed") + experienceOrderMapper.countByStatus("completed");
        conversionStats.put("experienceConversionRate", calcRate(experiencePaid, experienceTotal));
        conversionStats.put("experienceOrders", experienceTotal);
        conversionStats.put("experiencePaidOrders", experiencePaid);

        int totalPaid = ticketPaid + foodPaid + hotelPaid + productPaid + experiencePaid;
        conversionStats.put("overallConversionRate", calcRate(totalPaid, totalOrders));
        conversionStats.put("totalPaidOrders", totalPaid);

        // 模拟PV/UV（无埋点系统）
        conversionStats.put("pv", totalOrders * 8);
        conversionStats.put("uv", totalOrders * 4);
        conversionStats.put("pvToOrderRate", calcRate(totalOrders, totalOrders * 8));
        conversionStats.put("uvToOrderRate", calcRate(totalOrders, totalOrders * 4));

        stats.put("conversionStats", conversionStats);

        // 最近订单动态（每种取最近2条）
        List<Map<String, Object>> recentOrders = new ArrayList<>();
        addRecentOrders(recentOrders, ticketOrderMapper.findRecent(3), "ticket");
        addRecentOrders(recentOrders, foodOrderMapper.findRecent(3), "food");
        addRecentOrders(recentOrders, hotelOrderMapper.findRecent(3), "hotel");
        addRecentOrders(recentOrders, productOrderMapper.findRecent(3), "product");
        addRecentOrders(recentOrders, experienceOrderMapper.findRecent(3), "experience");
        // 按时间倒序排列
        recentOrders.sort((a, b) -> {
            LocalDateTime ta = (LocalDateTime) a.get("createdAt");
            LocalDateTime tb = (LocalDateTime) b.get("createdAt");
            return tb.compareTo(ta);
        });
        stats.put("recentOrders", recentOrders.subList(0, Math.min(recentOrders.size(), 8)));

        return Result.success(stats);
    }

    private void addRecentOrders(List<Map<String, Object>> list, List<?> orders, String type) {
        String typeLabel;
        switch (type) {
            case "ticket": typeLabel = "门票"; break;
            case "food": typeLabel = "美食"; break;
            case "hotel": typeLabel = "酒店"; break;
            case "product": typeLabel = "商品"; break;
            case "experience": typeLabel = "非遗体验"; break;
            default: typeLabel = "订单";
        }
        for (Object order : orders) {
            Map<String, Object> item = new LinkedHashMap<>();
            String itemName = "";
            BigDecimal totalPrice = BigDecimal.ZERO;
            LocalDateTime createdAt = null;
            String status = "";
            Long userId = 0L;

            if (order instanceof TicketOrder) {
                TicketOrder o = (TicketOrder) order;
                itemName = o.getTicketName();
                totalPrice = o.getTotalPrice();
                createdAt = o.getCreatedAt();
                status = o.getStatus();
                userId = o.getUserId();
            } else if (order instanceof FoodOrder) {
                FoodOrder o = (FoodOrder) order;
                itemName = o.getFoodName();
                totalPrice = o.getTotalPrice();
                createdAt = o.getCreatedAt();
                status = o.getStatus();
                userId = o.getUserId();
            } else if (order instanceof HotelOrder) {
                HotelOrder o = (HotelOrder) order;
                itemName = o.getHotelName();
                totalPrice = o.getTotalPrice();
                createdAt = o.getCreatedAt();
                status = o.getStatus();
                userId = o.getUserId();
            } else if (order instanceof ProductOrder) {
                ProductOrder o = (ProductOrder) order;
                itemName = o.getProductName();
                totalPrice = o.getTotalPrice();
                createdAt = o.getCreatedAt();
                status = o.getStatus();
                userId = o.getUserId();
            } else if (order instanceof ExperienceOrder) {
                ExperienceOrder o = (ExperienceOrder) order;
                itemName = o.getHeritageName();
                totalPrice = o.getTotalPrice();
                createdAt = o.getCreatedAt();
                status = o.getStatus();
                userId = o.getUserId();
            }

            item.put("type", type);
            item.put("typeLabel", typeLabel);
            item.put("itemName", itemName);
            item.put("totalPrice", totalPrice);
            item.put("createdAt", createdAt);
            item.put("status", status);
            item.put("userId", userId);
            item.put("timeAgo", formatTimeAgo(createdAt));
            list.add(item);
        }
    }

    private String formatTimeAgo(LocalDateTime time) {
        if (time == null) return "未知";
        long minutes = ChronoUnit.MINUTES.between(time, LocalDateTime.now());
        if (minutes < 1) return "刚刚";
        if (minutes < 60) return minutes + "分钟前";
        long hours = minutes / 60;
        if (hours < 24) return hours + "小时前";
        long days = hours / 24;
        return days + "天前";
    }

    private int countByStatus(String status) {
        int count = ticketOrderMapper.countByStatus(status) +
               foodOrderMapper.countByStatus(status) +
               hotelOrderMapper.countByStatus(status) +
               productOrderMapper.countByStatus(status);
        // experience orders use "confirmed" instead of "paid"
        if ("paid".equals(status)) {
            count += experienceOrderMapper.countByStatus("confirmed");
        } else {
            count += experienceOrderMapper.countByStatus(status);
        }
        return count;
    }

    private BigDecimal calcRate(int part, int total) {
        if (total == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(part * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
    }
}
