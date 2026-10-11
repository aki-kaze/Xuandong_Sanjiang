package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.Activity;
import org.example.sanjiang.mapper.ActivityMapper;
import org.example.sanjiang.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private ActivityMapper activityMapper;

    @Override
    public List<Activity> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return activityMapper.findAll();
    }

    @Override
    public List<Activity> findByStatus(String status, int page, int size) {
        PageHelper.startPage(page, size);
        return activityMapper.findByStatus(status);
    }

    @Override
    public List<Activity> findPublished(int page, int size) {
        PageHelper.startPage(page, size);
        return activityMapper.findPublished();
    }

    @Override
    public Activity findById(Long id) {
        return activityMapper.findById(id);
    }

    @Override
    public boolean save(Activity activity) {
        return activityMapper.insert(activity) > 0;
    }

    @Override
    public boolean update(Activity activity) {
        return activityMapper.update(activity) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return activityMapper.deleteById(id) > 0;
    }

    @Override
    public boolean publish(Long id) {
        return activityMapper.updatePublishStatus(id, 1) > 0;
    }

    @Override
    public boolean unpublish(Long id) {
        return activityMapper.updatePublishStatus(id, 0) > 0;
    }

    @Override
    public int count() {
        return activityMapper.countAll();
    }
}
