package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.Moment;
import org.example.sanjiang.mapper.MomentMapper;
import org.example.sanjiang.service.MomentService;
import org.example.sanjiang.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MomentServiceImpl implements MomentService {

    @Autowired
    private MomentMapper momentMapper;

    @Autowired(required = false)
    private SensitiveWordService sensitiveWordService;

    @Override
    public List<Moment> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return momentMapper.findAll(null, null);
    }

    @Override
    public List<Moment> findAll(int page, int size, String searchKey, Integer status) {
        PageHelper.startPage(page, size);
        return momentMapper.findAll(searchKey, status);
    }

    @Override
    public List<Moment> findByStatus(Integer status, int page, int size) {
        PageHelper.startPage(page, size);
        return momentMapper.findByStatus(status);
    }

    @Override
    public List<Moment> findByUserId(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        return momentMapper.findByUserId(userId);
    }

    @Override
    public Moment findById(Long id) {
        return momentMapper.findById(id);
    }

    @Override
    public boolean save(Moment moment) {
        // 敏感词过滤
        if (sensitiveWordService != null && moment.getContent() != null) {
            List<String> sensitiveWords = sensitiveWordService.checkText(moment.getContent());
            if (!sensitiveWords.isEmpty()) {
                // 包含敏感词，自动过滤替换
                moment.setContent(sensitiveWordService.filterText(moment.getContent()));
            }
        }
        if (moment.getStatus() == null) moment.setStatus(0); // 新帖子默认待审核
        if (moment.getLikes() == null) moment.setLikes(0);
        if (moment.getComments() == null) moment.setComments(0);
        if (moment.getTags() == null) moment.setTags("[]");
        return momentMapper.insert(moment) > 0;
    }

    @Override
    public boolean update(Moment moment) {
        return momentMapper.update(moment) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return momentMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateLikes(Long id, int delta) {
        return momentMapper.updateLikes(id, delta) > 0;
    }

    @Override
    public boolean updateComments(Long id, int delta) {
        return momentMapper.updateComments(id, delta) > 0;
    }

    @Override
    public int count() {
        return momentMapper.countAll();
    }

    @Override
    public int countByStatus(Integer status) {
        return momentMapper.countByStatus(status);
    }

    @Override
    public boolean approve(Long id) {
        return momentMapper.updateStatus(id, 1) > 0;
    }

    @Override
    public boolean reject(Long id) {
        return momentMapper.updateStatus(id, 2) > 0; // 2:已拒绝
    }
}
