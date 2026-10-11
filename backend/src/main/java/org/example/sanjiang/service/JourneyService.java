package org.example.sanjiang.service;

import org.example.sanjiang.entity.Journey;
import java.util.List;

public interface JourneyService {
    List<Journey> findAll(int page, int size);
    List<Journey> findByUserId(Long userId, int page, int size);
    Journey findById(Long id);
    boolean save(Journey journey);
    boolean update(Journey journey);
    boolean deleteById(Long id);
    int count();
    int countByUserId(Long userId);
}
