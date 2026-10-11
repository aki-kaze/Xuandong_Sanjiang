package org.example.sanjiang.mapper;

import org.example.sanjiang.entity.Route;
import java.util.List;

public interface RouteMapper {
    List<Route> findAll();
    List<Route> findAllAdmin();
    List<Route> findByDifficulty(String difficulty);
    List<Route> search(String keyword);
    Route findById(Integer id);
    int insert(Route route);
    int update(Route route);
    int deleteById(Integer id);
    int countAll();
    int countAllAdmin();
    int countByDifficulty(String difficulty);
    int countByDifficultyAndKeyword(String difficulty, String keyword);
    int countByKeyword(String keyword);
}
