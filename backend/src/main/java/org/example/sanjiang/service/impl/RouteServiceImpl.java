package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.Route;
import org.example.sanjiang.mapper.RouteMapper;
import org.example.sanjiang.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RouteServiceImpl implements RouteService {

    @Autowired
    private RouteMapper routeMapper;

    @Override
    public List<Route> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return routeMapper.findAll();
    }

    @Override
    public List<Route> findAllAdmin(int page, int size) {
        PageHelper.startPage(page, size);
        return routeMapper.findAllAdmin();
    }

    @Override
    public List<Route> findAll(int page, int size, String difficulty, String keyword) {
        PageHelper.startPage(page, size);
        if (difficulty != null && !difficulty.isEmpty()) {
            return routeMapper.findByDifficulty(difficulty);
        }
        if (keyword != null && !keyword.isEmpty()) {
            return routeMapper.search(keyword);
        }
        return routeMapper.findAll();
    }

    @Override
    public Route findById(Integer id) {
        return routeMapper.findById(id);
    }

    @Override
    public boolean save(Route route) {
        route.setCreatedAt(LocalDateTime.now());
        route.setUpdatedAt(LocalDateTime.now());
        if (route.getStatus() == null) {
            route.setStatus(1);
        }
        return routeMapper.insert(route) > 0;
    }

    @Override
    public boolean update(Route route) {
        route.setUpdatedAt(LocalDateTime.now());
        return routeMapper.update(route) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return routeMapper.deleteById(id) > 0;
    }

    @Override
    public int count() {
        return routeMapper.countAll();
    }

    @Override
    public int countAdmin() {
        return routeMapper.countAllAdmin();
    }

    @Override
    public int count(String difficulty, String keyword) {
        if (difficulty != null && !difficulty.isEmpty() && keyword != null && !keyword.isEmpty()) {
            return routeMapper.countByDifficultyAndKeyword(difficulty, keyword);
        }
        if (difficulty != null && !difficulty.isEmpty()) {
            return routeMapper.countByDifficulty(difficulty);
        }
        if (keyword != null && !keyword.isEmpty()) {
            return routeMapper.countByKeyword(keyword);
        }
        return routeMapper.countAll();
    }
}
