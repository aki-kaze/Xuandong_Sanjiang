package org.example.sanjiang.service;

import org.example.sanjiang.entity.Admin;
import java.util.Map;

public interface AdminService {
    Admin login(String username, String password);
    Admin findById(Integer id);
    Admin findByUsername(String username);
    boolean save(Admin admin);
    boolean update(Admin admin);
}
