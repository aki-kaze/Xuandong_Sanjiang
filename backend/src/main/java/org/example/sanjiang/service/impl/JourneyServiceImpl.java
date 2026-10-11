package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.Journey;
import org.example.sanjiang.mapper.JourneyMapper;
import org.example.sanjiang.service.JourneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class JourneyServiceImpl implements JourneyService {

    @Autowired
    private JourneyMapper journeyMapper;

    @Override
    public List<Journey> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return journeyMapper.findAll();
    }

    @Override
    public List<Journey> findByUserId(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        return journeyMapper.findByUserId(userId);
    }

    @Override
    public Journey findById(Long id) {
        return journeyMapper.findById(id);
    }

    @Override
    public boolean save(Journey journey) {
        return journeyMapper.insert(journey) > 0;
    }

    @Override
    public boolean update(Journey journey) {
        return journeyMapper.update(journey) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return journeyMapper.deleteById(id) > 0;
    }

    @Override
    public int count() {
        return journeyMapper.countAll();
    }

    @Override
    public int countByUserId(Long userId) {
        return journeyMapper.countByUserId(userId);
    }
}
