package org.example.sanjiang.mapper;

import org.example.sanjiang.entity.Admin;
import java.util.List;

public interface AdminMapper {
    List<Admin> findAll();
    Admin findById(Integer id);
    Admin findByUsername(String username);
    int insert(Admin admin);
    int update(Admin admin);
    int updateLastLogin(Integer id);
    int deleteById(Integer id);
    int countAll();
}
