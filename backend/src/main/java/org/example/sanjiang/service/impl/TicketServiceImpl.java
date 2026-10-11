package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.Ticket;
import org.example.sanjiang.mapper.TicketMapper;
import org.example.sanjiang.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketMapper ticketMapper;

    @Override
    public List<Ticket> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return ticketMapper.findAll();
    }

    @Override
    public List<Ticket> findAll(int page, int size, String type, String keyword) {
        PageHelper.startPage(page, size);
        // 类型+关键词组合筛选
        if (type != null && !type.isEmpty() && keyword != null && !keyword.isEmpty()) {
            return ticketMapper.findByTypeAndKeyword(type, keyword);
        }
        // 类型筛选
        if (type != null && !type.isEmpty()) {
            return ticketMapper.findByType(type);
        }
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            return ticketMapper.search(keyword);
        }
        return ticketMapper.findAll();
    }

    @Override
    public List<Ticket> findAllAdmin(int page, int size) {
        PageHelper.startPage(page, size);
        return ticketMapper.findAllAdmin();
    }

    @Override
    public Ticket findById(Integer id) {
        return ticketMapper.findById(id);
    }

    @Override
    public boolean save(Ticket ticket) {
        return ticketMapper.insert(ticket) > 0;
    }

    @Override
    public boolean update(Ticket ticket) {
        return ticketMapper.update(ticket) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return ticketMapper.deleteById(id) > 0;
    }

    @Override
    public int count() {
        return ticketMapper.countAll();
    }

    @Override
    public int count(String type, String keyword) {
        // 类型+关键词组合统计
        if (type != null && !type.isEmpty() && keyword != null && !keyword.isEmpty()) {
            return ticketMapper.countByTypeAndKeyword(type, keyword);
        }
        // 类型统计
        if (type != null && !type.isEmpty()) {
            return ticketMapper.countByType(type);
        }
        // 关键词统计
        if (keyword != null && !keyword.isEmpty()) {
            return ticketMapper.countByKeyword(keyword);
        }
        // 无筛选条件，统计全部
        return ticketMapper.countAll();
    }

    @Override
    public int countAdmin() {
        return ticketMapper.countAllAdmin();
    }
}
