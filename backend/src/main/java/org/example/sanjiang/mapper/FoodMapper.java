package org.example.sanjiang.mapper;

import org.example.sanjiang.entity.Food;
import java.util.List;

public interface FoodMapper {
    List<Food> findAll();
    List<Food> findAllAdmin();
    List<Food> findByCategory(String category);
    List<Food> findByCategoryAndKeyword(String category, String keyword);
    List<Food> search(String keyword);
    Food findById(Integer id);
    int insert(Food food);
    int update(Food food);
    int deleteById(Integer id);
    int countAll();
    int countAllAdmin();
    int countByCategory(String category);
    int countByCategoryAndKeyword(String category, String keyword);
    int countByKeyword(String keyword);
}
