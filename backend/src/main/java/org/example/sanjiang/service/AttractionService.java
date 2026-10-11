package org.example.sanjiang.service;

import org.example.sanjiang.entity.Attraction;
import java.util.List;

public interface AttractionService {
    List<Attraction> findAll(int page, int size);
    List<Attraction> findAllAdmin(int page, int size);
    List<Attraction> findAll(int page, int size, String category, String keyword);
    Attraction findById(Integer id);
    List<Attraction> search(String keyword);
    boolean save(Attraction attraction);
    boolean update(Attraction attraction);
    boolean deleteById(Integer id);
    int count();
    int countAdmin();
    int count(String category, String keyword);
}
