package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.Guide;
import org.example.sanjiang.mapper.GuideMapper;
import org.example.sanjiang.service.GuideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GuideServiceImpl implements GuideService {

    @Autowired
    private GuideMapper guideMapper;

    @Override
    public List<Guide> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return guideMapper.findAll();
    }

    @Override
    public List<Guide> findByStatus(Integer status, int page, int size) {
        PageHelper.startPage(page, size);
        return guideMapper.findByStatus(status);
    }

    @Override
    public List<Guide> searchByTitle(String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        return guideMapper.searchByTitle(keyword);
    }

    @Override
    public Guide findById(Long id) {
        return guideMapper.findById(id);
    }

    @Override
    public List<Guide> findByUserId(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        return guideMapper.findByUserId(userId);
    }

    @Override
    public boolean save(Guide guide) {
        return guideMapper.insert(guide) > 0;
    }

    @Override
    public boolean update(Guide guide) {
        return guideMapper.update(guide) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return guideMapper.deleteById(id) > 0;
    }

    @Override
    public int count() {
        return guideMapper.countAll();
    }

    @Override
    public List<Guide> findByAuditStatus(Integer auditStatus, int page, int size) {
        PageHelper.startPage(page, size);
        return guideMapper.findByAuditStatus(auditStatus);
    }

    @Override
    public int countByAuditStatus(Integer auditStatus) {
        return guideMapper.countByAuditStatus(auditStatus);
    }

    @Override
    public int countByTitle(String keyword) {
        return guideMapper.countByTitle(keyword);
    }
}
