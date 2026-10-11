package org.example.sanjiang.service;

import org.example.sanjiang.entity.Heritage;
import java.util.List;

public interface HeritageService {
    List<Heritage> findAll(int page, int size);
    List<Heritage> findAllAdmin(int page, int size);
    List<Heritage> findAll(int page, int size, String level, String keyword);
    Heritage findById(Integer id);
    boolean save(Heritage heritage);
    boolean update(Heritage heritage);
    boolean deleteById(Integer id);
    int count();
    int countAdmin();
    int count(String level, String keyword);
}
