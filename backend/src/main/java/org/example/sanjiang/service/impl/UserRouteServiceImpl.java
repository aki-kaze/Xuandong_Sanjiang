package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.UserRoute;
import org.example.sanjiang.mapper.UserRouteMapper;
import org.example.sanjiang.service.SensitiveWordService;
import org.example.sanjiang.service.UserRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class UserRouteServiceImpl implements UserRouteService {

    @Autowired
    private UserRouteMapper userRouteMapper;

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Override
    public List<UserRoute> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return userRouteMapper.findAll();
    }

    @Override
    public List<UserRoute> findByConditions(int page, int size, String keyword, Integer userId, String status) {
        PageHelper.startPage(page, size);
        return userRouteMapper.findByConditions(keyword, userId, status);
    }

    @Override
    public List<UserRoute> findByUserId(Integer userId) {
        return userRouteMapper.findByUserId(userId);
    }

    @Override
    public UserRoute findById(Integer id) {
        return userRouteMapper.findById(id);
    }

    @Override
    public boolean save(UserRoute userRoute) {
        userRoute.setCreatedAt(LocalDateTime.now());
        userRoute.setUpdatedAt(LocalDateTime.now());
        if (userRoute.getStatus() == null) {
            userRoute.setStatus(1);
        }
        return userRouteMapper.insert(userRoute) > 0;
    }

    @Override
    public List<String> checkRouteDescription(String description) {
        if (description == null || description.isEmpty()) {
            // ✅ Java 8 正确空列表
            return Collections.emptyList();
        }
        return sensitiveWordService.checkText(description);
    }

    @Override
    public boolean update(UserRoute userRoute) {
        userRoute.setUpdatedAt(LocalDateTime.now());
        return userRouteMapper.update(userRoute) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return userRouteMapper.deleteById(id) > 0;
    }

    @Override
    public int count() {
        return userRouteMapper.countAll();
    }
}
