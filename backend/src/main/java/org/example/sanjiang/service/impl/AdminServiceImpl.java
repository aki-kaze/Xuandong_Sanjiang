package org.example.sanjiang.service.impl;

import org.example.sanjiang.entity.Admin;
import org.example.sanjiang.mapper.AdminMapper;
import org.example.sanjiang.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public Admin login(String username, String password) {
        Admin admin = adminMapper.findByUsername(username);

        if (admin == null) {
            return null;
        }

        if (!admin.getPassword().equals(password)) {
            return null;
        }

        if (admin.getStatus() == 0) {
            return null;
        }

        adminMapper.updateLastLogin(admin.getId());
        return admin;
    }

    @Override
    public Admin findById(Integer id) {
        return adminMapper.findById(id);
    }

    @Override
    public Admin findByUsername(String username) {
        return adminMapper.findByUsername(username);
    }

    @Override
    public boolean save(Admin admin) {
        return adminMapper.insert(admin) > 0;
    }

    @Override
    public boolean update(Admin admin) {
        return adminMapper.update(admin) > 0;
    }
}
