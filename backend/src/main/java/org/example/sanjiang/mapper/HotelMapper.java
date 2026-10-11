package org.example.sanjiang.mapper;

import org.example.sanjiang.entity.Hotel;
import java.util.List;

public interface HotelMapper {
    List<Hotel> findAll();
    List<Hotel> findAllAdmin();
    List<Hotel> findByType(String type);
    List<Hotel> findByTypeAndKeyword(String type, String keyword);
    List<Hotel> search(String keyword);
    Hotel findById(Integer id);
    int insert(Hotel hotel);
    int update(Hotel hotel);
    int deleteById(Integer id);
    int countAll();
    int countAllAdmin();
    int countByType(String type);
    int countByTypeAndKeyword(String type, String keyword);
    int countByKeyword(String keyword);
}
