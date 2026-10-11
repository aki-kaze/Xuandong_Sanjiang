package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.Food;
import org.example.sanjiang.mapper.FoodMapper;
import org.example.sanjiang.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FoodServiceImpl implements FoodService {

    @Autowired
    private FoodMapper foodMapper;

    @Override
    public List<Food> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return foodMapper.findAll();
    }

    @Override
    public List<Food> findAll(int page, int size, String category, String keyword) {
        PageHelper.startPage(page, size);
        // 分类+关键词组合筛选
        if (category != null && !category.isEmpty() && keyword != null && !keyword.isEmpty()) {
            return foodMapper.findByCategoryAndKeyword(category, keyword);
        }
        // 分类筛选
        if (category != null && !category.isEmpty()) {
            return foodMapper.findByCategory(category);
        }
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            return foodMapper.search(keyword);
        }
        return foodMapper.findAll();
    }

    @Override
    public List<Food> findAllAdmin(int page, int size) {
        PageHelper.startPage(page, size);
        return foodMapper.findAllAdmin();
    }

    @Override
    public Food findById(Integer id) {
        return foodMapper.findById(id);
    }

    @Override
    public boolean save(Food food) {
        return foodMapper.insert(food) > 0;
    }

    @Override
    public boolean update(Food food) {
        return foodMapper.update(food) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return foodMapper.deleteById(id) > 0;
    }

    @Override
    public int count() {
        return foodMapper.countAll();
    }

    @Override
    public int count(String category, String keyword) {
        // 分类+关键词组合统计
        if (category != null && !category.isEmpty() && keyword != null && !keyword.isEmpty()) {
            return foodMapper.countByCategoryAndKeyword(category, keyword);
        }
        // 分类统计
        if (category != null && !category.isEmpty()) {
            return foodMapper.countByCategory(category);
        }
        // 关键词统计
        if (keyword != null && !keyword.isEmpty()) {
            return foodMapper.countByKeyword(keyword);
        }
        // 无筛选条件，统计全部
        return foodMapper.countAll();
    }

    @Override
    public int countAdmin() {
        return foodMapper.countAllAdmin();
    }
}
