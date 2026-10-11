package org.example.sanjiang.ai;

import org.example.sanjiang.entity.Attraction;
import org.example.sanjiang.entity.Food;
import org.example.sanjiang.mapper.AttractionMapper;
import org.example.sanjiang.mapper.FoodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MCPToolManager {

    @Autowired(required = false)
    private AttractionMapper attractionMapper;

    @Autowired(required = false)
    private FoodMapper foodMapper;

    public static class ToolResult {
        public String type;
        public String content;
        public Object data;
        public boolean toolResult = true;
        public String toolType;
        public Object toolData;
        public Map<String, Object> itinerary;
        public String reply;
        public boolean isToolCall = false;

        public static ToolResult from(String type, String content, Object data) {
            ToolResult result = new ToolResult();
            result.type = type;
            result.content = content;
            result.data = data;
            result.toolResult = true;
            result.toolType = type;
            result.toolData = data;
            result.isToolCall = true;
            return result;
        }
    }

    public ToolResult executeTool(String text, Map<String, Object> context) {
        String lowerText = text.toLowerCase();

        // 只保留需要跳转页面的工具
        if (isTicketQuery(lowerText)) {
            return ticketTool(text, context);
        }
        if (isTrafficQuery(lowerText)) {
            return trafficTool(text, context);
        }
        if (isHotelQuery(lowerText)) {
            return hotelTool(text, context);
        }

        // 其他所有问题都返回 null，让 AI 直接回答
        return null;
    }

    private boolean isAttractionQuery(String text) {
        String[] keywords = {"三江景点", "景点推荐", "必去景点", "有哪些景点", "游玩", "景区", "古镇推荐", "茶园", "鼓楼", "风雨桥", "程阳八寨", "布央", "丹洲", "月也侗寨"};
        return containsAny(text, keywords);
    }

    private boolean isFoodQuery(String text) {
        String[] keywords = {"三江美食", "三江吃什么", "侗族美食", "三江餐馆", "油茶", "酸鱼", "酸肉", "侗族糯米饭"};
        return containsAny(text, keywords);
    }

    private boolean isItineraryQuery(String text) {
        String[] keywords = {"三江一日游", "三江两日游", "三江三日游", "三江行程", "三江怎么玩", "三江游玩路线"};
        return containsAny(text, keywords);
    }

    private boolean isWeatherQuery(String text) {
        String[] keywords = {"天气", "温度", "气温", "下雨", "气候", "穿什么"};
        return containsAny(text, keywords);
    }

    private boolean isHeritageQuery(String text) {
        String[] keywords = {"三江非遗", "侗族非遗", "侗族大歌", "三江风雨桥", "三江鼓楼"};
        return containsAny(text, keywords);
    }

    private boolean isTicketQuery(String text) {
        String[] keywords = {"三江门票", "景区购票", "三江买票", "三江票价"};
        return containsAny(text, keywords);
    }

    private boolean isTrafficQuery(String text) {
        String[] keywords = {"去三江", "三江交通", "怎么去三江", "三江导航", "到三江"};
        return containsAny(text, keywords);
    }

    private boolean isHotelQuery(String text) {
        String[] keywords = {"三江住宿", "三江酒店", "住哪里", "民宿", "客栈", "住什么", "住宿推荐"};
        return containsAny(text, keywords);
    }

    private boolean containsAny(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public ToolResult attractionTool(String text, Map<String, Object> context) {
        List<Map<String, Object>> list = new ArrayList<>();

        if (attractionMapper != null) {
            try {
                List<Attraction> attractions = attractionMapper.findAll();
                for (Attraction a : attractions) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", a.getId());
                    item.put("name", a.getName());
                    item.put("rating", a.getRating());
                    item.put("price", a.getPrice());
                    item.put("description", a.getDescription());
                    item.put("cover", a.getCover());
                    item.put("category", a.getCategory());
                    list.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (list.isEmpty()) {
            list.add(createDefaultAttraction("程阳八寨", "AAAA级景区", 80));
            list.add(createDefaultAttraction("丹洲古镇", "中国唯一的水上古镇", 40));
            list.add(createDefaultAttraction("三江大侗寨", "侗族文化标志", 30));
            list.add(createDefaultAttraction("布央茶园", "中国最美茶园", 0));
            list.add(createDefaultAttraction("程阳风雨桥", "世界四大名桥之一", 15));
        }

        if (text.contains("必去") || text.contains("推荐") || text.contains("好玩")) {
            list = list.subList(0, Math.min(5, list.size()));
        }

        return ToolResult.from("attraction", "为您推荐三江必去景点👇", list);
    }

    private Map<String, Object> createDefaultAttraction(String name, String desc, int price) {
        Map<String, Object> a = new HashMap<>();
        a.put("id", 0);
        a.put("name", name);
        a.put("rating", 4.5);
        a.put("price", price);
        a.put("description", desc);
        a.put("cover", "");
        a.put("category", "景点");
        return a;
    }

    public ToolResult foodTool(String text, Map<String, Object> context) {
        List<Map<String, Object>> list = new ArrayList<>();

        if (foodMapper != null) {
            try {
                List<Food> foods = foodMapper.findAll();
                for (Food f : foods) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", f.getId());
                    item.put("name", f.getName());
                    item.put("price", f.getPrice());
                    item.put("description", f.getDescription());
                    item.put("cover", f.getCover());
                    item.put("category", f.getCategory());
                    list.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (list.isEmpty()) {
            list.add(createDefaultFood("油茶", "侗族待客必备", 10));
            list.add(createDefaultFood("酸鱼", "侗族传统美食", 38));
            list.add(createDefaultFood("酸肉", "开胃下饭", 32));
            list.add(createDefaultFood("侗族糯米饭", "香糯可口", 15));
            list.add(createDefaultFood("百家宴", "侗族最高礼遇", 60));
        }

        return ToolResult.from("food", "为您推荐三江特色美食👇", list);
    }

    private Map<String, Object> createDefaultFood(String name, String desc, int price) {
        Map<String, Object> f = new HashMap<>();
        f.put("id", 0);
        f.put("name", name);
        f.put("price", price);
        f.put("description", desc);
        f.put("cover", "");
        f.put("category", "美食");
        return f;
    }

    public ToolResult itineraryTool(String text, Map<String, Object> context) {
        Map<String, Object> itinerary = new HashMap<>();
        itinerary.put("title", "三江经典一日游推荐路线");
        itinerary.put("route", Arrays.asList(
            "上午：程阳八寨 - 探访侗族古村落",
            "中午：品尝侗族特色美食（油茶、酸鱼）",
            "下午：三江大侗寨 - 侗族文化体验",
            "傍晚：程阳风雨桥 - 赏夕阳"
        ));
        itinerary.put("budget", "约200元/人（含门票+餐费）");

        ToolResult result = new ToolResult();
        result.type = "itinerary";
        result.content = "已为您规划推荐路线👇";
        result.itinerary = itinerary;
        result.isToolCall = true;
        return result;
    }

    public ToolResult weatherTool(String text, Map<String, Object> context) {
        Map<String, Object> weatherData = new HashMap<>();
        weatherData.put("temp", "22");
        weatherData.put("weather", "多云");
        weatherData.put("humidity", "72");
        weatherData.put("wind", "东北风2级");

        return ToolResult.from("weather", "三江今日天气👇", weatherData);
    }

    public ToolResult heritageTool(String text, Map<String, Object> context) {
        ToolResult result = new ToolResult();
        result.type = "heritage";
        result.content = "为您介绍三江非遗文化，点击下方按钮前往非遗页面了解更多👇";
        result.isToolCall = true;
        return result;
    }

    public ToolResult ticketTool(String text, Map<String, Object> context) {
        ToolResult result = new ToolResult();
        result.type = "ticket";
        result.content = "为您提供三江景点购票信息，点击下方按钮前往购票页面👇";
        result.isToolCall = true;
        return result;
    }

    public ToolResult trafficTool(String text, Map<String, Object> context) {
        ToolResult result = new ToolResult();
        result.type = "traffic";
        result.content = "为您提供三江交通指南：\n\n🚗 自驾：从柳州出发约2小时车程\n🚌 公交：县城内有公交线路连接各景点\n🚕 打车：起步价5元，各景点间约10-30元\n\n点击下方按钮前往地图页面查看详细路线👇";
        result.isToolCall = true;
        return result;
    }

    public ToolResult hotelTool(String text, Map<String, Object> context) {
        ToolResult result = new ToolResult();
        result.type = "hotel";
        result.content = "为您推荐三江住宿推荐👇\n\n🏨 **三江县城**：交通便利，酒店齐全，价格100-300元/晚\n🏡 **程阳八寨**：推荐侗族特色民宿，体验当地文化，80-200元/晚\n🌿 **布央茶园周边**：生态民宿，空气清新，可体验采茶\n\n点击下方按钮跳转查看更多住宿详情👇";
        result.isToolCall = true;
        return result;
    }
}
