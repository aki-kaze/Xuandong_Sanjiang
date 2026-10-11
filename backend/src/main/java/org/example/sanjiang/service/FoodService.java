package org.example.sanjiang.service;

import org.example.sanjiang.entity.Food;
import java.util.List;

public interface FoodService {
    List<Food> findAll(int page, int size);
    List<Food> findAll(int page, int size, String category, String keyword);
    List<Food> findAllAdmin(int page, int size);
    Food findById(Integer id);
    boolean save(Food food);
    boolean update(Food food);
    boolean deleteById(Integer id);
    int count();
    int count(String category, String keyword);
    int countAdmin();
}
