package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.Attraction;
import org.example.sanjiang.mapper.AttractionMapper;
import org.example.sanjiang.service.AttractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AttractionServiceImpl implements AttractionService {

    @Autowired
    private AttractionMapper attractionMapper;

    @Override
    public List<Attraction> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return attractionMapper.findAll();
    }

    @Override
    public List<Attraction> findAllAdmin(int page, int size) {
        PageHelper.startPage(page, size);
        return attractionMapper.findAllAdmin();
    }

    @Override
    public List<Attraction> findAll(int page, int size, String category, String keyword) {
        PageHelper.startPage(page, size);
        // 分类+关键词组合筛选
        if (category != null && !category.isEmpty() && keyword != null && !keyword.isEmpty()) {
            return attractionMapper.findByCategoryAndKeyword(category, keyword);
        }
        // 分类筛选
        if (category != null && !category.isEmpty()) {
            return attractionMapper.findByCategory(category);
        }
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            return attractionMapper.search(keyword);
        }
        // 无筛选条件，返回全部
        return attractionMapper.findAll();
    }

    @Override
    public Attraction findById(Integer id) {
        return attractionMapper.findById(id);
    }

    @Override
    public List<Attraction> search(String keyword) {
        return attractionMapper.search(keyword);
    }

    @Override
    public boolean save(Attraction attraction) {
        return attractionMapper.insert(attraction) > 0;
    }

    @Override
    public boolean update(Attraction attraction) {
        return attractionMapper.update(attraction) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return attractionMapper.deleteById(id) > 0;
    }

    @Override
    public int count() {
        return attractionMapper.countAll();
    }

    @Override
    public int countAdmin() {
        return attractionMapper.countAllAdmin();
    }

    @Override
    public int count(String category, String keyword) {
        // 分类+关键词组合统计
        if (category != null && !category.isEmpty() && keyword != null && !keyword.isEmpty()) {
            return attractionMapper.countByCategoryAndKeyword(category, keyword);
        }
        // 分类统计
        if (category != null && !category.isEmpty()) {
            return attractionMapper.countByCategory(category);
        }
        // 关键词统计
        if (keyword != null && !keyword.isEmpty()) {
            return attractionMapper.countByKeyword(keyword);
        }
        // 无筛选条件，统计全部
        return attractionMapper.countAll();
    }
}
