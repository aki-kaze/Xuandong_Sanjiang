package org.example.sanjiang.service;

import org.example.sanjiang.entity.Route;
import java.util.List;

public interface RouteService {
    List<Route> findAll(int page, int size);
    List<Route> findAllAdmin(int page, int size);
    List<Route> findAll(int page, int size, String difficulty, String keyword);
    Route findById(Integer id);
    boolean save(Route route);
    boolean update(Route route);
    boolean deleteById(Integer id);
    int count();
    int countAdmin();
    int count(String difficulty, String keyword);
}
