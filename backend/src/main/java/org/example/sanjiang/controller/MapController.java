package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.Attraction;
import org.example.sanjiang.entity.Heritage;
import org.example.sanjiang.entity.Activity;
import org.example.sanjiang.service.AttractionService;
import org.example.sanjiang.service.HeritageService;
import org.example.sanjiang.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/map")
@CrossOrigin
public class MapController {

    @Autowired
    private AttractionService attractionService;
    @Autowired
    private HeritageService heritageService;
    @Autowired
    private ActivityService activityService;

    @GetMapping("/data")
    public Result<Map<String, Object>> getMapData() {
        // 获取所有有坐标的景点
        List<Attraction> attractions = attractionService.findAll(1, 100, null, null);
        List<Map<String, Object>> attractionMarkers = new ArrayList<>();
        for (Attraction a : attractions) {
            if (a.getLat() != null && a.getLng() != null) {
                Map<String, Object> marker = new HashMap<>();
                marker.put("id", a.getId());
                marker.put("name", a.getName());
                marker.put("lat", a.getLat());
                marker.put("lng", a.getLng());
                marker.put("type", "attraction");
                marker.put("cover", a.getCover());
                marker.put("address", a.getAddress());
                marker.put("price", a.getPrice());
                marker.put("category", a.getCategory());
                attractionMarkers.add(marker);
            }
        }

        // 获取所有有坐标的非遗
        List<Heritage> heritages = heritageService.findAll(1, 100);
        List<Map<String, Object>> heritageMarkers = new ArrayList<>();
        for (Heritage h : heritages) {
            if (h.getLat() != null && h.getLng() != null) {
                Map<String, Object> marker = new HashMap<>();
                marker.put("id", h.getId());
                marker.put("name", h.getName());
                marker.put("lat", h.getLat());
                marker.put("lng", h.getLng());
                marker.put("type", "heritage");
                marker.put("cover", h.getCover());
                marker.put("level", h.getLevel());
                marker.put("description", h.getDescription());
                heritageMarkers.add(marker);
            }
        }

        // 获取所有有坐标的活动
        List<Activity> activities = activityService.findAll(1, 100);
        List<Map<String, Object>> activityMarkers = new ArrayList<>();
        for (Activity a : activities) {
            if (a.getLat() != null && a.getLng() != null) {
                Map<String, Object> marker = new HashMap<>();
                marker.put("id", a.getId());
                marker.put("title", a.getTitle());
                marker.put("lat", a.getLat());
                marker.put("lng", a.getLng());
                marker.put("type", "activity");
                marker.put("cover", a.getCover());
                marker.put("location", a.getLocation());
                marker.put("status", a.getStatus());
                activityMarkers.add(marker);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("attractions", attractionMarkers);
        result.put("heritages", heritageMarkers);
        result.put("activities", activityMarkers);
        Map<String, Object> center = new HashMap<>();
        center.put("lat", 25.7750);
        center.put("lng", 109.6030);
        result.put("center", center);
        return Result.success(result);
    }
}
