package org.example.sanjiang.service;

import org.example.sanjiang.entity.Hotel;
import java.util.List;

public interface HotelService {
    List<Hotel> findAll(int page, int size);
    List<Hotel> findAllAdmin(int page, int size);
    List<Hotel> findAll(int page, int size, String type, String keyword);
    Hotel findById(Integer id);
    boolean save(Hotel hotel);
    boolean update(Hotel hotel);
    boolean deleteById(Integer id);
    int count();
    int countAdmin();
    int count(String type, String keyword);
}
