package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.Heritage;
import org.example.sanjiang.mapper.HeritageMapper;
import org.example.sanjiang.service.HeritageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HeritageServiceImpl implements HeritageService {

    @Autowired
    private HeritageMapper heritageMapper;

    @Override
    public List<Heritage> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return heritageMapper.findAll();
    }

    @Override
    public List<Heritage> findAllAdmin(int page, int size) {
        PageHelper.startPage(page, size);
        return heritageMapper.findAllAdmin();
    }

    @Override
    public List<Heritage> findAll(int page, int size, String level, String keyword) {
        PageHelper.startPage(page, size);
        // 级别+关键词组合筛选
        if (level != null && !level.isEmpty() && keyword != null && !keyword.isEmpty()) {
            return heritageMapper.findByLevelAndKeyword(level, keyword);
        }
        // 级别筛选
        if (level != null && !level.isEmpty()) {
            return heritageMapper.findByLevel(level);
        }
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            return heritageMapper.search(keyword);
        }
        return heritageMapper.findAll();
    }

    @Override
    public Heritage findById(Integer id) {
        return heritageMapper.findById(id);
    }

    @Override
    public boolean save(Heritage heritage) {
        return heritageMapper.insert(heritage) > 0;
    }

    @Override
    public boolean update(Heritage heritage) {
        return heritageMapper.update(heritage) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return heritageMapper.deleteById(id) > 0;
    }

    @Override
    public int count() {
        return heritageMapper.countAll();
    }

    @Override
    public int countAdmin() {
        return heritageMapper.countAllAdmin();
    }

    @Override
    public int count(String level, String keyword) {
        // 级别+关键词组合统计
        if (level != null && !level.isEmpty() && keyword != null && !keyword.isEmpty()) {
            return heritageMapper.countByLevelAndKeyword(level, keyword);
        }
        // 级别统计
        if (level != null && !level.isEmpty()) {
            return heritageMapper.countByLevel(level);
        }
        // 关键词统计
        if (keyword != null && !keyword.isEmpty()) {
            return heritageMapper.countByKeyword(keyword);
        }
        // 无筛选条件，统计全部
        return heritageMapper.countAll();
    }
}
