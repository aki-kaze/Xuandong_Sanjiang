package org.example.sanjiang.mapper;

import org.example.sanjiang.entity.Journey;
import java.util.List;

public interface JourneyMapper {
    List<Journey> findAll();
    List<Journey> findByUserId(Long userId);
    Journey findById(Long id);
    int insert(Journey journey);
    int update(Journey journey);
    int deleteById(Long id);
    int countAll();
    int countByUserId(Long userId);
}
