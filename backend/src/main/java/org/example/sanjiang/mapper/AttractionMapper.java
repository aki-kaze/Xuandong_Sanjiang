package org.example.sanjiang.mapper;

import org.example.sanjiang.entity.Attraction;
import java.util.List;

public interface AttractionMapper {
    List<Attraction> findAll();
    List<Attraction> findAllAdmin();
    List<Attraction> findByCategory(String category);
    List<Attraction> findByCategoryAndKeyword(String category, String keyword);
    List<Attraction> search(String keyword);
    Attraction findById(Integer id);
    int insert(Attraction attraction);
    int update(Attraction attraction);
    int deleteById(Integer id);
    int countAll();
    int countAllAdmin();
    int countByCategory(String category);
    int countByCategoryAndKeyword(String category, String keyword);
    int countByKeyword(String keyword);
}
