package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.Hotel;
import org.example.sanjiang.mapper.HotelMapper;
import org.example.sanjiang.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HotelServiceImpl implements HotelService {

    @Autowired
    private HotelMapper hotelMapper;

    @Override
    public List<Hotel> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return hotelMapper.findAll();
    }

    @Override
    public List<Hotel> findAllAdmin(int page, int size) {
        PageHelper.startPage(page, size);
        return hotelMapper.findAllAdmin();
    }

    @Override
    public List<Hotel> findAll(int page, int size, String type, String keyword) {
        PageHelper.startPage(page, size);
        // 类型+关键词组合筛选
        if (type != null && !type.isEmpty() && keyword != null && !keyword.isEmpty()) {
            return hotelMapper.findByTypeAndKeyword(type, keyword);
        }
        // 类型筛选
        if (type != null && !type.isEmpty()) {
            return hotelMapper.findByType(type);
        }
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            return hotelMapper.search(keyword);
        }
        return hotelMapper.findAll();
    }

    @Override
    public Hotel findById(Integer id) {
        return hotelMapper.findById(id);
    }

    @Override
    public boolean save(Hotel hotel) {
        return hotelMapper.insert(hotel) > 0;
    }

    @Override
    public boolean update(Hotel hotel) {
        return hotelMapper.update(hotel) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return hotelMapper.deleteById(id) > 0;
    }

    @Override
    public int count() {
        return hotelMapper.countAll();
    }

    @Override
    public int countAdmin() {
        return hotelMapper.countAllAdmin();
    }

    @Override
    public int count(String type, String keyword) {
        // 类型+关键词组合统计
        if (type != null && !type.isEmpty() && keyword != null && !keyword.isEmpty()) {
            return hotelMapper.countByTypeAndKeyword(type, keyword);
        }
        // 类型统计
        if (type != null && !type.isEmpty()) {
            return hotelMapper.countByType(type);
        }
        // 关键词统计
        if (keyword != null && !keyword.isEmpty()) {
            return hotelMapper.countByKeyword(keyword);
        }
        // 无筛选条件，统计全部
        return hotelMapper.countAll();
    }
}
